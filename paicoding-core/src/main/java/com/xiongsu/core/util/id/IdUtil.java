package com.xiongsu.core.util.id;

import com.xiongsu.api.enums.pay.ThirdPayWayEnum;
import com.xiongsu.core.async.AsyncUtil;
import com.xiongsu.core.util.CompressUtil;
import com.xiongsu.core.util.DateUtil;
import com.xiongsu.core.util.id.snowflake.PaiSnowflakeIdGenerator;
import com.xiongsu.core.util.id.snowflake.SnowflakeProducer;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.atomic.AtomicLong;

import static com.xiongsu.core.util.CompressUtil.int2str;

/**
 * @author YiHui
 * @date 2023/8/30
 */
public class IdUtil {
    /**
     * 默认的id生成器
     */
    public static SnowflakeProducer DEFAULT_ID_PRODUCER = new SnowflakeProducer(new PaiSnowflakeIdGenerator());

    private static AtomicLong INCR = new AtomicLong((int) (Math.random() * 500));
    private static long lastTime = 0;


    /**
     * 生成全局id
     *
     * @return
     */
    public static Long genId() {
        return DEFAULT_ID_PRODUCER.genId();
    }

    /**
     * 生成字符串格式全局id
     *
     * @return
     */
    public static String genStrId() {
        return CompressUtil.int2str(genId());
    }


    /**
     * 生成支付的唯一code
     * 简化的规则：payWay前缀 + 年月日+时分秒
     *
     * @return
     */
    public static String genPayCode(ThirdPayWayEnum payWay, Long id) {
        long now = System.currentTimeMillis();
        if (DateUtil.skipDay(lastTime, now)) {
            lastTime = now;
            INCR.set((int) (Math.random() * 500));
        }
        return payWay.getPrefix() + String.format("%06d", INCR.addAndGet(1)) + "-" + id;
    }

    /**
     * 根据payCode 解析获取 payId
     *
     * @param code
     * @return
     */
    public static Long getPayIdFromPayCode(String code) {
        String[] str = StringUtils.split(code, "-");
        return Long.valueOf(str[str.length - 1]);
    }

    public static void main(String[] args) {
        System.out.println(IdUtil.genStrId());
        Long id = IdUtil.genId();
        System.out.println(id + " = " + int2str(id));
        System.out.println(IdUtil.genId() + "->" + IdUtil.genStrId());
        AsyncUtil.sleep(2000);
        System.out.println(IdUtil.genId() + "->" + IdUtil.genStrId());

        System.out.println("-----");

        SnowflakeProducer producer = new SnowflakeProducer(new PaiSnowflakeIdGenerator());
        id = producer.genId();
        System.out.println("id: " + id + " -> " + int2str(id));
        AsyncUtil.sleep(3000L);
        id = producer.genId();
        System.out.println("id: " + id + " -> " + int2str(id));
    }
}
