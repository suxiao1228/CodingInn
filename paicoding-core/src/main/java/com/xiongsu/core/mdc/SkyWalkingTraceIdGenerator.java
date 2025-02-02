package com.xiongsu.core.mdc;

import com.google.common.base.Joiner;

import java.util.UUID;

/**
 * SkyWalking的traceId生成策略
 * <p>
 * 源码：<a href="https://github.com/apache/skywalking-java/blob/ddc68e27e2764ca6299f04ef21a5d864bf660deb/apm-sniffer/apm-agent-core/src/main/java/org/apache/skywalking/apm/agent/core/context/ids/GlobalIdGenerator.java"/>
 *
 * @author YiHui
 * @date 2023/5/29
 */
public class SkyWalkingTraceIdGenerator {
    private static final String PROCESS_ID = UUID.randomUUID().toString().replaceAll("-", "");//生成一个全局唯一的UUID,去掉"-"
    private static final ThreadLocal<IDContext> THREAD_ID_SEQUENCE = ThreadLocal.withInitial(
            () -> new IDContext(System.currentTimeMillis(), (short) 0));//ThreadLocal 在每个线程首次使用时会创建一个新的 IDContext 实例。
    // IDContext 构造时接受当前的时间戳和初始化的 short 类型的线程序列号（从 0 开始）。

    private SkyWalkingTraceIdGenerator() {
    }

    /**
     * Generate a new id, combined by three parts.
     * <p>
     * The first one represents application instance id.
     * <p>
     * The second one represents thread id.
     * <p>
     * The third one also has two parts, 1) a timestamp, measured in milliseconds 2) a seq, in current thread, between
     * 0(included) and 9999(included)
     *
     * @return unique id to represent a trace or segment
     */
    public static String generate() {//生成traceId
        return Joiner.on(".").join(
                PROCESS_ID,//应用实例Id
                String.valueOf(Thread.currentThread().getId()),//当前线程ID
                String.valueOf(THREAD_ID_SEQUENCE.get().nextSeq())//当前线程的时间戳和自增序列
        );
    }

    private static class IDContext {//这个类用于管理每个线程的时间戳和线程序列号
        private static final int MAX_SEQ = 10_000;//定义线程序列的最大值为 10000
        private long lastTimestamp;//时间戳：lastTimestamp 存储上次生成 ID 时的时间戳（毫秒
        private short threadSeq;//线程序列号：threadSeq 存储当前线程的自增序列号，初始为 0。

        // Just for considering time-shift-back only.
        private long lastShiftTimestamp;//时间回拨时间戳：用于记录发生时间回拨时的时间戳。lastShiftTimestamp 会在时间回拨时进行更新，帮助调整序列号。
        private int lastShiftValue;//时间回拨修正值：在时间回拨时，lastShiftValue 用来调整序列号的修正值，确保生成的 ID 仍然唯一。

        private IDContext(long lastTimestamp, short threadSeq) {
            this.lastTimestamp = lastTimestamp;
            this.threadSeq = threadSeq;
        }

        private long nextSeq() {//生成下一个序列号
            return timestamp() * 10000 + nextThreadSeq();
        }

        private long timestamp() {//获取当前时间戳：
            long currentTimeMillis = System.currentTimeMillis();

            if (currentTimeMillis < lastTimestamp) {
                // Just for considering time-shift-back by Ops or OS. @hanahmily 's suggestion.
                if (lastShiftTimestamp != currentTimeMillis) {
                    lastShiftValue++;
                    lastShiftTimestamp = currentTimeMillis;
                }
                return lastShiftValue;
            } else {
                lastTimestamp = currentTimeMillis;
                return lastTimestamp;
            }
        }

        private short nextThreadSeq() {//生成线程序列号
            if (threadSeq == MAX_SEQ) {
                threadSeq = 0;
            }
            return threadSeq++;
        }
    }
}
