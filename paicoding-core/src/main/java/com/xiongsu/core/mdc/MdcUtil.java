package com.xiongsu.core.mdc;

import org.slf4j.MDC;

/**
 * @author YiHui
 * @date 2023/5/29
 */
public class MdcUtil {
    public static final String TRACE_ID_KEY = "traceId";

    public static void add(String key, String val) {
        MDC.put(key, val);
    }

    public static void addTraceId() {//这是另一个静态方法，目的是向 MDC 中添加 traceId
        // traceId的生成规则，技术派提供了两种生成策略，可以使用自定义的也可以使用SkyWalking; 实际项目中选择一种即可
        MDC.put(TRACE_ID_KEY, SelfTraceIdGenerator.generate());
    }

    public static String getTraceId() {//用来从 MDC 中获取当前线程的 traceId。
        return MDC.get(TRACE_ID_KEY);
    }

    public static void reset() {//该方法会重置 MDC 中的内容。首先，它会保存当前的 traceId，然后清除 MDC 中的所有信息，最后再将之前保存的 traceId 恢复到 MDC 中。
        String traceId = MDC.get(TRACE_ID_KEY);
        MDC.clear();
        MDC.put(TRACE_ID_KEY, traceId);
    }

    public static void clear() {// 清空当前线程的所有 MDC 数据，确保日志上下文为空。
        MDC.clear();
    }
}
