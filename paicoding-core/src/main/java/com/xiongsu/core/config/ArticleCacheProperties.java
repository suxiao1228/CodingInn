package com.xiongsu.core.config;

import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 控制热点文章的缓存访问参数
 */
@Data
@Valid

//@ConfigurationProperties 是 Spring Boot 提供的核心注解，用于将外部配置文件（如 application.yml 或 application.properties）中的属性批量绑定到 Java 对象。
// 通过 prefix 指定配置项的前缀，可以结构化地管理配置，避免分散的 @Value 注解
@ConfigurationProperties(prefix = "hot.article.cache")
public class ArticleCacheProperties {

    /**
     * 热点文章缓存的过期时间，默认为1天，单位为参
     */
    private Long expireSeconds = 60 * 60 * 24L;

    /**
     * 热点文章缓存的最大数量，默认为10
     */
    private Integer maximumSize = 10;

}
