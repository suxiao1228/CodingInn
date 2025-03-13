package com.xiongsu.core.util.id.snowflake;

public interface IdGenerator {
    /**
     * 生成分布式Id
     */
    Long nextId();
}
