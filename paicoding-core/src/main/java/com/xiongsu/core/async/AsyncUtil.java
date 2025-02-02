package com.xiongsu.core.async;

import cn.hutool.core.thread.ExecutorBuilder;
import cn.hutool.core.util.ArrayUtil;
import com.alibaba.ttl.TransmittableThreadLocal;
import com.alibaba.ttl.threadpool.TtlExecutors;
import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.xiongsu.core.util.EnvUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.io.Closeable;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

@Slf4j
public class AsyncUtil {
    //TransmittableThreadLocal 是一种线程局部变量，能够跨线程传递变量。与 ThreadLocal 相比，TransmittableThreadLocal 支持在多线程环境下（例如线程池中的任务切换）传递线程变量。
    private static final TransmittableThreadLocal<CompletableFutureBridge> THREAD_LOCAL = new TransmittableThreadLocal<>();
    private static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {
        private final ThreadFactory defaultFactory = Executors.defaultThreadFactory();
        private final AtomicInteger threadNumber = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            Thread thread = this.defaultFactory.newThread(r);//创建一个新的线程，并将r作为执行内容
            if (!thread.isDaemon()) {
                thread.setDaemon(true);//将其设置为守护线程
            }

            thread.setName("paicoding-" + this.threadNumber.getAndIncrement());//每个线程的名字是paicoding+ 一个递增的数字   确保线程独一无二
            return thread;
        }
    };
    private static ExecutorService executorService;//线程池，负责执行异步任务
    private static SimpleTimeLimiter simpleTimeLimiter;//限制任务执行时间，防止任务无限阻塞

    static {
        initExecutorService(Runtime.getRuntime().availableProcessors() * 2, 50);//类加载时自动执行，初始化线程池，线程池大小为 CPU 核心数 * 2，最大 50 个线程。
    }
    public static void initExecutorService(int core, int max) {
        // 异步工具类的默认线程池构建, 参数选择原则:
        //  1. 技术派不存在cpu密集型任务，大部分操作都设计到 redis/mysql 等io操作
        //  2. 统一的异步封装工具，这里的线程池是一个公共的执行仓库，不希望被其他的线程执行影响，因此队列长度为0, 核心线程数满就创建线程执行，超过最大线程，就直接当前线程执行
        //  3. 同样因为属于通用工具类，再加上技术派的异步使用的情况实际上并不是非常饱和的，因此空闲线程直接回收掉即可；大部分场景下，cpu * 2的线程数即可满足要求了
        max = Math.max(core, max);
        executorService = new ExecutorBuilder() //线程池构建
                .setCorePoolSize(core)//设置核心线程数
                .setMaxPoolSize(max)//设置最大线程数
                .setKeepAliveTime(0)//非核心线程空闲 0s 直接销毁，避免资源浪费
                .setKeepAliveTime(0, TimeUnit.SECONDS)//非核心线程空闲 0s 直接销毁，避免资源浪费。
                .setWorkQueue(new SynchronousQueue<Runnable>())//无缓冲队列，任务必须直接被线程处理，否则新建线程执行。
                .setHandler(new ThreadPoolExecutor.CallerRunsPolicy())//拒绝策略： 线程池满了，任务由当前线程执行，避免任务丢失。
                .setThreadFactory(THREAD_FACTORY)//自定义线程工厂，给线程命名，方便管理。
                .buildFinalizable();
        // 包装一下线程池，避免出现上下文复用场景
        executorService = TtlExecutors.getTtlExecutorService(executorService);//解决线程池中的「线程上下文丢失」问题
        simpleTimeLimiter = SimpleTimeLimiter.create(executorService);//限制任务执行时间，防止任务超时导致线程阻塞。
    }
    /**
     * 带超时时间的方法调用执行，当执行时间超过给定的时间，则返回一个超时异常，内部的任务还是正常执行
     * 若超时时间内执行完毕，则直接返回
     *
     * @param time
     * @param unit
     * @param call
     * @param <T>
     * @return
     */
    public static <T> T callWithTimeLimit(long time, TimeUnit unit, Callable<T> call) throws ExecutionException, InterruptedException, TimeoutException {
        return simpleTimeLimiter.callWithTimeout(call, time, unit);
    }


    public static void execute(Runnable call) {
        executorService.execute(call);
    }

    public static <T> Future<T> submit(Callable<T> t) {
        return executorService.submit(t);
    }


    public static boolean sleep(Number timeout, TimeUnit timeUnit) {
        try {
            timeUnit.sleep(timeout.longValue());
            return true;
        } catch (InterruptedException var3) {
            return false;
        }
    }

    public static boolean sleep(Number millis) {
        return millis == null ? true : sleep(millis.longValue());
    }

    public static boolean sleep(long millis) {
        if (millis > 0L) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException var3) {
                return false;
            }
        }

        return true;
    }


    public static class CompletableFutureBridge implements Closeable {
        private List<CompletableFuture> list;
        private Map<String, Long> cost;
        private String taskName;
        private boolean markOver;
        private ExecutorService executorService;

        public CompletableFutureBridge() {
            this(AsyncUtil.executorService, "CompletableFutureExecute");
        }

        public CompletableFutureBridge(ExecutorService executorService, String task) {
            this.taskName = task;
            list = new CopyOnWriteArrayList<>();
            // 支持排序的耗时记录
            cost = new ConcurrentSkipListMap<>();
            cost.put(task, System.currentTimeMillis());
            this.executorService = TtlExecutors.getTtlExecutorService(executorService);
            this.markOver = true;
        }

        /**
         * 异步执行，带返回结果
         *
         * @param supplier 执行任务
         * @param name     耗时标识
         * @return
         */
        public <T> CompletableFutureBridge async(Supplier<T> supplier, String name) {
            list.add(CompletableFuture.supplyAsync(supplyWithTime(supplier, name), this.executorService));
            return this;
        }

        /**
         * 同步执行，待返回结果
         *
         * @param supplier 执行任务
         * @param name     耗时标识
         * @param <T>      返回类型
         * @return 任务的执行返回结果
         */
        public <T> T sync(Supplier<T> supplier, String name) {
            return supplyWithTime(supplier, name).get();
        }

        /**
         * 异步执行，无返回结果
         *
         * @param run  执行任务
         * @param name 耗时标识
         * @return
         */
        public CompletableFutureBridge async(Runnable run, String name) {
            list.add(CompletableFuture.runAsync(runWithTime(run, name), this.executorService));
            return this;
        }

        /**
         * 同步执行，无返回结果
         *
         * @param run  执行任务
         * @param name 耗时标识
         * @return
         */
        public CompletableFutureBridge sync(Runnable run, String name) {
            runWithTime(run, name).run();
            return this;
        }

        private Runnable runWithTime(Runnable run, String name) {
            return () -> {
                startRecord(name);
                try {
                    run.run();
                } finally {
                    endRecord(name);
                }
            };
        }

        private <T> Supplier<T> supplyWithTime(Supplier<T> call, String name) {
            return () -> {
                startRecord(name);
                try {
                    return call.get();
                } finally {
                    endRecord(name);
                }
            };
        }

        public CompletableFutureBridge allExecuted() {
            if (!CollectionUtils.isEmpty(list)) {
                CompletableFuture.allOf(ArrayUtil.toArray(list, CompletableFuture.class)).join();
            }
            this.markOver = true;
            endRecord(this.taskName);
            return this;
        }

        private void startRecord(String name) {
            cost.put(name, System.currentTimeMillis());
        }

        private void endRecord(String name) {
            long now = System.currentTimeMillis();
            long last = cost.getOrDefault(name, now);
            if (last >= now / 1000) {
                // 之前存储的是时间戳，因此我们需要更新成执行耗时 ms单位
                cost.put(name, now - last);
            }
        }

        public void prettyPrint() {
            if (EnvUtil.isPro()) {
                // 生产环境默认不打印执行耗时日志
                return;
            }

            if (!this.markOver) {
                // 在格式化输出时，要求所有任务执行完毕
                this.allExecuted();
            }

            StringBuilder sb = new StringBuilder();
            sb.append('\n');
            long totalCost = cost.remove(taskName);
            sb.append("StopWatch '").append(taskName).append("': running time = ").append(totalCost).append(" ms");
            sb.append('\n');
            if (cost.size() <= 1) {
                sb.append("No task info kept");
            } else {
                sb.append("---------------------------------------------\n");
                sb.append("ms         %     Task name\n");
                sb.append("---------------------------------------------\n");
                NumberFormat pf = NumberFormat.getPercentInstance();
                pf.setMinimumIntegerDigits(2);
                pf.setMinimumFractionDigits(2);
                pf.setGroupingUsed(false);
                for (Map.Entry<String, Long> entry : cost.entrySet()) {
                    sb.append(entry.getValue()).append("\t\t");
                    sb.append(pf.format(entry.getValue() / (double) totalCost)).append("\t\t");
                    sb.append(entry.getKey()).append("\n");
                }
            }

            log.info("\n---------------------\n{}\n--------------------\n", sb);
        }

        @Override
        public void close() {
            try {
                if (!this.markOver) {
                    // 做一个兜底，避免业务侧没有手动结束，导致异步任务没有执行完就提前返回结果
                    this.allExecuted();
                }

                AsyncUtil.release();
                prettyPrint();
            } catch (Exception e) {
                log.error("释放耗时上下文异常! {}", taskName, e);
            }
        }
    }

    public static CompletableFutureBridge concurrentExecutor(String... name) {
        if (name.length > 0) {
            return new CompletableFutureBridge(AsyncUtil.executorService, name[0]);
        }
        return new CompletableFutureBridge();
    }

    /**
     * 开始桥接类
     *
     * @param executorService 线程池
     * @param name            标记名
     * @return 桥接类
     */
    public static CompletableFutureBridge startBridge(ExecutorService executorService, String name) {
        CompletableFutureBridge bridge = new CompletableFutureBridge(executorService, name);
        THREAD_LOCAL.set(bridge);
        return bridge;
    }

    /**
     * 获取计时桥接类
     *
     * @return 桥接类
     */
    public static CompletableFutureBridge getBridge() {
        return THREAD_LOCAL.get();
    }

    /**
     * 释放统计
     */
    public static void release() {
        THREAD_LOCAL.remove();
    }
}
