package com.xiongsu.core.util.id.snowflake;

import com.xiongsu.core.util.DateUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * 基于雪花算法计算的id生成器
 *
 * @author YiHui
 * @date 2023/8/30
 */
@Slf4j
public class SnowflakeProducer {
    private BlockingQueue<Long> queue;

    /**
     * id失效的间隔时间
     */
    public static final Long ID_EXPIRE_TIME_INTER = DateUtil.ONE_DAY_MILL;//定义ID失效的间隔时间
    private static final int QUEUE_SIZE = 10;//设置队列的最大容量。这个队列最多可以存放10个ID，用来缓冲生成的ID。队列大小限制了生成ID的并发数量，避免在高负载情况下阻塞太多操作
    private ExecutorService es = Executors.newSingleThreadExecutor((Runnable r) -> {//创建一个单线程的 ExecutorService。这里用来管理生成ID的线程，确保只有一个线程在后台循环生成ID，避免多个线程并发生成ID导致的线程安全问题。
        // 线程名设置为 "SnowflakeProducer-generate-thread"，并且设置为守护线程，确保当主程序退出时这个线程会自动结束。
        Thread t = new Thread(r);
        t.setName("SnowflakeProducer-generate-thread");
        t.setDaemon(true);
        return t;
    });

    public SnowflakeProducer(final IdGenerator generator) {//接受一个 IdGenerator 对象，表示生成ID的核心逻辑。
        queue = new LinkedBlockingQueue<>(QUEUE_SIZE);
        es.submit(() -> {
            long lastTime = System.currentTimeMillis();
            while (true) {
                try {//生成id，并放入队列
                    queue.offer(generator.nextId(), 1, TimeUnit.MINUTES);
                } catch (InterruptedException e1) {
                } catch (Exception e) {
                    log.info("gen id error! {}", e.getMessage());
                }

                // 当出现跨天时，自动重置业务id
                try {
                    long now = System.currentTimeMillis();
                    if (now / ID_EXPIRE_TIME_INTER - lastTime / ID_EXPIRE_TIME_INTER > 0) {
                        // 跨天，清空队列
                        queue.clear();
                        log.info("清空id队列，重新设置");
                    }
                    lastTime = now;

                } catch (Exception e) {
                    log.info("auto remove illegal ids error! {}", e.getMessage());
                }
            }
        });
    }

    public Long genId() {//生成ID的接口。这个方法从队列中获取一个ID并返回。queue.take() 会阻塞，直到队列中有ID可用。如果在等待过程中出现异常，会抛出运行时异常。
        try {
            return queue.take();
        } catch (InterruptedException e) {
            log.error("雪花算法生成逻辑异常", e);
            throw new RuntimeException("雪花算法生成id异常!", e);
        }
    }
}
