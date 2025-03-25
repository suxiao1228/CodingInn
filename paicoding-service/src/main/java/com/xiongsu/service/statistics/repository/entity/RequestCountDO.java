package com.xiongsu.service.statistics.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xiongsu.api.entity.BaseDO;
import com.xiongsu.api.enums.cache.CacheTypeEnum;
import com.xiongsu.core.cache.annotation.CacheKey;
import com.xiongsu.core.cache.annotation.CacheType;
import com.xiongsu.core.cache.annotation.CacheValue;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.Date;

/**
 * 请求计数表
 */
@Data
@EqualsAndHashCode(callSuper = true)//是 Lombok 提供的一个注解，主要用于自动生成 equals() 和 hashCode() 方法。
@TableName("request_count")
@CacheType(value = CacheTypeEnum.CACHE_HASH)
public class RequestCountDO extends BaseDO {

    private static final long serialVersionUID = 1L;

    /**
     * 机器Id
     */
    @CacheKey
    private String host;

    /**
     * 访问计数
     */
    @CacheValue
    private Integer cnt;

    /**
     * 当前日期
     */
    private Date date;
}
