package com.xiongsu.core.mdc;

import org.slf4j.MDC;



public class MdcUtil {

    //MDC（Mapped Diagnostic Context） 是 SLF4J 和 Logback
    // 等日志框架提供的一种线程绑定的上下文变量存储机制，用于在日志中记录和跟踪与当前线程相关的上下文信息。


   //日志上下文管理： MDC 允许为每个线程存储特定的上下文数据，
    // 例如 用户 ID、请求 ID、会话 ID、交易编号 等，方便后续日志追踪。

    public static final String TRACE_ID_KEY = "traceId";

    public static void add(String key, String val) {
        MDC.put(key, val);
    }

    public static void addTraceId() {
        // traceId的生成规则，技术派提供了两种生成策略，可以使用自定义的也可以使用SkyWalking; 实际项目中选择一种即可
        MDC.put(TRACE_ID_KEY, SelfTraceIdGenerator.generate());
    }

    public static String genTraceId() {
        return MDC.get(TRACE_ID_KEY);
    }

    public static void reset() {
        String traceId = MDC.get(TRACE_ID_KEY);
        MDC.clear();
        MDC.put(TRACE_ID_KEY, traceId);
    }

    public static void clear() {
        MDC.clear();
    }
}
