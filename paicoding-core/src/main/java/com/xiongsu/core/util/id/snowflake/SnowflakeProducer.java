package com.xiongsu.core.util.id.snowflake;


import com.xiongsu.core.util.DateUtil;
import lombok.extern.slf4j.Slf4j;


import java.util.concurrent.*;

/**
 * 基于雪花算法计算的id生成器
 */
@Slf4j
public class SnowflakeProducer {
    private BlockingQueue<Long> queue;

    /**
     * id失效的间隔时间
     */
    //使用 Executors.newSingleThreadExecutor() 创建单线程池
    //只运行一个线程，这个线程会 不断生成 ID
    //守护线程（Daemon Thread）：当 JVM 退出时，线程自动终止，防止阻止程序退出
    public static final Long ID_EXPIRE_TIME_INTER = DateUtil.ONE_DAY_MILL;
    private static final int QUEUE_SIZE = 10;
    private ExecutorService es = Executors.newSingleThreadExecutor( (Runnable r) ->{
        Thread t = new Thread(r);
        t.setName("SnowflakeProducer-generate-thread");
        t.setDaemon(true);
        return t;
    });

    //生产ID
    public SnowflakeProducer(final IdGenerator generator) {
        queue = new LinkedBlockingQueue<>(QUEUE_SIZE);
        es.submit(() -> {
            long lastTime = System.currentTimeMillis(); //// 记录上一次的时间戳
            while (true) {
                try {
                    queue.offer(generator.nextId(), 1, TimeUnit.MINUTES); // 生成ID并放入队列
                } catch (InterruptedException e1) {
                } catch (Exception e) {
                    log.info("gen id error {}", e.getMessage());
                }

                //当出现跨天时候，自动重置业务id
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

    public Long genId() {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            log.error("雪花算法生成逻辑异常", e);
            throw new RuntimeException("雪花算法生成id异常!", e);
        }
    }
}
