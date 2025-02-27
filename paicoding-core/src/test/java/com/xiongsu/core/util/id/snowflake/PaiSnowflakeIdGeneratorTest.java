package com.xiongsu.core.util.id.snowflake;

import com.xiongsu.core.async.AsyncUtil;
import com.xiongsu.core.util.DateUtil;
import com.xiongsu.core.util.IpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.TreeSet;

@Slf4j
public class PaiSnowflakeIdGeneratorTest {
    /**
     * 自增序号位数
     */
    private static final long SEQUENCE_BITS = 10L;//自增序号占用的位数，这里是 10 位。

    /**
     * 机器位数
     */
    private static final long WORKER_ID_BITS = 7L;  //机器 ID 占用的位数，这里是 7 位。
    private static final long DATA_CENTER_BITS = 3L;//数据中心 ID 占用的位数，这里是 3 位。

    private static final long SEQUENCE_MASK = (1 << SEQUENCE_BITS) - 1;// 用于在生成 ID 时提取出序列号部分。由于 SEQUENCE_BITS 是 10，
    // 这里的 SEQUENCE_MASK 就是一个二进制值，最低的 10 位都是 1。


    private static final long WORKER_ID_LEFT_SHIFT_BITS = SEQUENCE_BITS;
    private static final long DATACENTER_LEFT_SHIFT_BITS = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final long TIMESTAMP_LEFT_SHIFT_BITS = WORKER_ID_LEFT_SHIFT_BITS + WORKER_ID_BITS + DATA_CENTER_BITS;
    /**
     * 机器id (7位）
     */
    private long workId = 1;
    /**
     * 数据中心 (3位)
     */
    private long dataCenter = 1;

    /**
     * 上次的访问时间
     */
    private long lastTime;
    /**
     * 自增序号
     */
    private long sequence;

    private byte sequenceOffset;

    public PaiSnowflakeIdGeneratorTest() {
        try {
            String ip = IpUtil.getLocalIp4Address();//获取本地IPv4地址
            String[] cells = StringUtils.split(ip, ".");
            this.dataCenter = Integer.parseInt(cells[0]) & ((1 << DATA_CENTER_BITS) - 1);
            this.workId = Integer.parseInt(cells[3]) >> 16 & ((1 << WORKER_ID_BITS) - 1);
        } catch (Exception e) {
            this.dataCenter = 1;
            this.workId = 1;
        }
    }

    public PaiSnowflakeIdGeneratorTest(int workId, int dateCenter) {
        this.workId = workId;
        this.dataCenter = dateCenter;
    }

    /**
     * 生成趋势自增的id
     *
     * @return
     */

    public synchronized Long nextId() {//用于生成 ID。它是 synchronized 的，保证了多线程环境下的线程安全。
        long nowTime = waitToIncrDiffIfNeed(getNowTime()); //getNowTime() 获取当前的时间戳（秒级）。
        //waitToIncrDiffIfNeed(nowTime) 检查当前时间与上次生成 ID 时的时间是否相同，如果时间相同，则尝试等待，确保不会出现时钟回拨或时间差异带来的问题。
        if (lastTime == nowTime) {
            if (0L == (sequence = (sequence + 1) & SEQUENCE_MASK)) {
                // 表示当前这一时刻的自增数被用完了；等待下一时间点
                nowTime = waitUntilNextTime(nowTime);
            }//如果当前时间和上次生成 ID 的时间相同，则尝试自增 sequence（序列号）。
            //sequence = (sequence + 1) & SEQUENCE_MASK：序列号加 1，并使用位掩码 SEQUENCE_MASK 保证序列号不会超过最大值。
            //如果序列号达到最大值（0），则等待到下一秒（或下一毫秒）再生成 ID，防止同一毫秒内生成重复 ID。
        } else {
            // 上一毫秒若以0作为序列号开始值，则这一秒以1为序列号开始值
            vibrateSequenceOffset();
            sequence = sequenceOffset;
            //如果当前时间与上次生成的时间不同，意味着进入了新的一毫秒或秒，序列号从 sequenceOffset 重新开始。
        }
        lastTime = nowTime;
        long ans = ((nowTime % DateUtil.ONE_DAY_SECONDS) << TIMESTAMP_LEFT_SHIFT_BITS)//时间戳
                | (dataCenter << DATACENTER_LEFT_SHIFT_BITS)//数据中心ID
                | (workId << WORKER_ID_LEFT_SHIFT_BITS)//工作机器ID
                | sequence;//序列号
        if (log.isDebugEnabled()) {
            log.debug("seconds:{}, datacenter:{}, work:{}, seq:{}, ans={}", nowTime % DateUtil.ONE_DAY_SECONDS, dataCenter, workId, sequence, ans);
        }
        return Long.parseLong(String.format("%s%011d", getDaySegment(nowTime), ans));
        //return ans;
        //getDaySegment(nowTime)：根据当前时间戳获取当天的分区信息（基于年月日）。
        //String.format("%s%011d", ...)：将日期分区和生成的 ID 拼接成一个最终的 64 位 ID。
    }

    /**
     * 若当前时间比上次执行时间要小，则等待时间追上来，避免出现时钟回拨导致的数据重复
     *
     * @param nowTime 当前时间戳
     * @return 返回新的时间戳
     */
    //如果当前时间戳小于上次生成 ID 的时间（通常是由于时钟回拨），则调用 AsyncUtil.sleep(diff) 等待时间回到正确的位置，避免重复的 ID。
    //lastTime <= nowTime 是为了防止时钟回拨带来的时间问题。
    private long waitToIncrDiffIfNeed(final long nowTime) {
        if (lastTime <= nowTime) {
            return nowTime;
        }
        long diff = lastTime - nowTime;
        AsyncUtil.sleep(diff);
        return getNowTime();
    }

    /**
     * 等待下一秒
     *
     * @param lastTime
     * @return
     */
    //如果同一毫秒内生成的序列号已经用完（即序列号已达到上限），则等待下一秒钟来继续生成新的 ID。
    private long waitUntilNextTime(final long lastTime) {
        long result = getNowTime();
        while (result <= lastTime) {
            result = getNowTime();
        }
        return result;
    }


    //该方法的作用是通过反转 sequenceOffset 的最低位来“震荡”序列号的起始值，这有助于避免在某些特殊情况下重复生成 ID。
    private void vibrateSequenceOffset() {
        sequenceOffset = (byte) (~sequenceOffset & 1);
    }


    /**
     * 获取当前时间
     *
     * @return 秒为单位
     */
    //获取当前的时间戳（秒级）。System.currentTimeMillis() 返回的是毫秒级时间戳，通过除以 1000 转换成秒级。
    private long getNowTime() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * 基于年月日构建分区
     *
     * @param time 时间戳
     * @return 时间分区
     */
    //这个方法通过当前时间的时间戳生成一个日期分区（YYDDD 格式），
    // YY 是年份的后两位，DDD 是当天的天数（1 到 365 或 366）。这个分区有助于将 ID 分成日期块，以便更好的分布和管理。
    private static String getDaySegment(long time) {
        LocalDateTime localDate = DateUtil.time2LocalTime(time * 1000L);
        return String.format("%02d%03d", localDate.getYear() % 100, localDate.getDayOfYear());
    }

    public static void main(String[] args) {
        PaiSnowflakeIdGeneratorTest snowflakeIdGenerator = new PaiSnowflakeIdGeneratorTest(1, 2);

        // 生成50个id
        Set<Long> set = new TreeSet<>();
        for (int i = 0; i < 50; i++) {
            set.add(snowflakeIdGenerator.nextId());
        }
        System.out.println(set.size());
        System.out.println(set);

        // 验证生成100万个id需要多久
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            snowflakeIdGenerator.nextId();
        }
        System.out.println(System.currentTimeMillis() - startTime);
    }
}