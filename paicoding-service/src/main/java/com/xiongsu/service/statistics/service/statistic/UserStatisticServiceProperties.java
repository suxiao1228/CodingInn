package com.xiongsu.service.statistics.service.statistic;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@Valid
@ConfigurationProperties(prefix = "online.statistics")
public class UserStatisticServiceProperties {

    @Getter
    public enum UserStatisticServiceType {
        /**
         * 使用caffeine缓存实现在线人数的统计
         */
        CAFFEINE("caffeine"),

        /**
         * 使用Redis实现在线人数的统计
         */
        REDIS("redis"),

        /**
         * 使用内存的原子整形AtomicInteger
         */
        ATOMIC_INTEGER("atomicInteger");

        private final String serviceType;


        UserStatisticServiceType(String serviceType) {
            this.serviceType = serviceType;
        }
    }

    @NotNull(message = "online.statistics.type 不能为空")
    private UserStatisticServiceType type;
}
