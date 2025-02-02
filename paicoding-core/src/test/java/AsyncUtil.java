import org.junit.Test;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;


public class AsyncUtil implements ThreadFactory {
    private final ThreadFactory defaultFactory = Thread::new; // 默认线程工厂
    private final AtomicInteger threadNumber = new AtomicInteger(1); // 线程计数

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = this.defaultFactory.newThread(r);
        if (!thread.isDaemon()) {
            thread.setDaemon(true);
        }
        thread.setName("paicoding-" + this.threadNumber.getAndIncrement());
        return thread;
    }

    public static void main(String[] args) {
        AsyncUtil factory = new AsyncUtil();
        Thread t1 = factory.newThread(() -> System.out.println(Thread.currentThread().getName() + " running"));
        Thread t2 = factory.newThread(() -> System.out.println(Thread.currentThread().getName() + " running"));

        t1.start();
        t2.start();
    }
}
