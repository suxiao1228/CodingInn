package com.xiongsu.core.async;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Target({ElementType.METHOD})//指定了该注解可以应用的 Java 元素类型。则该注解只能用于方法
@Retention(RetentionPolicy.RUNTIME) // 指定了该注解的保留策略，决定了该注解在哪个阶段可用。这个则表示注解会在运行时保留
@Documented// 注解 表示使用该注解的元素会包含在 Java 文档中。
public @interface AsyncExecute {
    /**
     * 是否开启异步执行
     */
    boolean value() default true;

    /**
     * 超时时间，默认3s
     */
    int timeOut() default 3;

    /**
     * 超时时间单位，默认秒
     */
    TimeUnit unit() default TimeUnit.SECONDS;

    /**
     * 当出现超时返回的兜底类型，支持SpEL
     * 如果返回的是空字符串，则表示抛出异常
     */
    String timeOutRsp() default "";
}
