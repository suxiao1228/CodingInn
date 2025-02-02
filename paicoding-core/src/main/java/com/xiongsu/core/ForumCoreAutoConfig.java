package com.xiongsu.core;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.xiongsu.core.cache.RedisClient;
import com.xiongsu.core.config.ProxyProperties;
import com.xiongsu.core.net.ProxyCenter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * @author YiHui
 * @date 2022/9/4
 */
@Configuration
@EnableConfigurationProperties(ProxyProperties.class)
@ComponentScan(basePackages = "com.xiongsu.core")
public class ForumCoreAutoConfig {
    @Autowired
    private ProxyProperties proxyProperties;

    public ForumCoreAutoConfig(RedisTemplate<String, String> redisTemplate) {
        RedisClient.register(redisTemplate);
    }

    /**
     * 定义缓存管理器，配合Spring的 @Cache 来使用
     *
     * @return
     */
    @Bean("caffeineCacheManager")
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder().
                // 设置过期时间，写入后五分钟过期
                        expireAfterWrite(5, TimeUnit.MINUTES)
                // 初始化缓存空间大小
                .initialCapacity(100)
                // 最大的缓存条数
                .maximumSize(200)
        );
        return cacheManager;
    }

    @PostConstruct
    public void init() {
        // 这里借助手动解析配置信息，并实例化为Java POJO对象，来实现代理池的初始化
        ProxyCenter.initProxyPool(proxyProperties.getProxy());
    }
}
//自动配置代理池：通过 ProxyProperties 配置类自动加载代理池的配置，并在应用启动时初始化代理池。
//Redis 配置：注册了一个 RedisTemplate，用于与 Redis 进行交互，并在构造函数中将其传递给 RedisClient 类。
//缓存管理：配置了一个基于 Caffeine 的缓存管理器，用于 Spring 缓存机制。
//自动扫描组件：通过 @ComponentScan 注解扫描指定包下的组件，自动加载和配置相关的服务和类。
//ForumCoreAutoConfig 类的目的是在 Spring Boot 启动时自动配置和初始化一些核心功能，包括代理池、Redis 客户端、缓存管理器等。
// 通过使用 Spring 的自动化配置机制，这个类简化了项目的配置过程，减少了手动配置的代码。
