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
    //队列缓冲机制：通过限制队列的大小（最多 10 个 ID）来避免过多的 ID 被同时生成，减少系统的压力。在高并发的情况下，队列能有效缓解生产与消费速度不一致的问题。
    //单线程生成：通过 ExecutorService 只使用一个线程负责生成 ID，确保 ID 生成是串行的，避免多个线程竞争导致的 ID 冲突或线程安全问题。
    //跨天处理：使用跨天检测机制，确保 ID 在跨天时不会重复生成，保持唯一性。
    //超时控制：在队列满时，使用 offer() 方法并设置超时，这样可以避免长时间阻塞。
    private BlockingQueue<Long> queue;

    /**
     * id失效的间隔时间
     */
    public static final Long ID_EXPIRE_TIME_INTER = DateUtil.ONE_DAY_MILL;//定义ID失效的间隔时间
    private static final int QUEUE_SIZE = 10;//设置队列的最大容量。这个队列最多可以存放10个ID，用来缓冲生成的ID。队列大小限制了生成ID的并发数量，避免在高负载情况下阻塞太多操作

    //使用 ExecutorService 来创建一个单线程的 线程池。Executors.newSingleThreadExecutor() 创建了一个单线程池，确保只有一个线程在后台循环生成 ID。
    //设置了线程名为 "SnowflakeProducer-generate-thread"，并且设置为 守护线程（setDaemon(true)），这意味着当主程序退出时，生成 ID 的线程会自动结束
    //这样做的目的是 避免并发问题，只有一个线程在后台生成 ID，避免多个线程同时生成 ID，造成 ID 冲突或者线程不安全的情况。
    private ExecutorService es = Executors.newSingleThreadExecutor((Runnable r) -> {//创建一个单线程的 ExecutorService。这里用来管理生成ID的线程，确保只有一个线程在后台循环生成ID，避免多个线程并发生成ID导致的线程安全问题。
        // 线程名设置为 "SnowflakeProducer-generate-thread"，并且设置为守护线程，确保当主程序退出时这个线程会自动结束。
        Thread t = new Thread(r);
        t.setName("SnowflakeProducer-generate-thread");
        t.setDaemon(true);
        return t;
    });


    //使用 queue.offer(generator.nextId(), 1, TimeUnit.MINUTES) 来将生成的 ID 放入队列。offer() 方法用于将元素放入队列，设置了 1 分钟的超时，防止队列满时长时间阻塞。
    //每次生成 ID 后，会检查是否跨天。ID_EXPIRE_TIME_INTER 代表的是 一天的时间，如果当前时间与上次生成 ID 的时间超过一天，表示已经跨天了。
    //如果是跨天，则通过 queue.clear() 清空队列，重新生成新的 ID，防止 ID 过期或重复。
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
