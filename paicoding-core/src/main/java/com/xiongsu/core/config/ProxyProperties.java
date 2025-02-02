package com.xiongsu.core.config;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.Proxy;
import java.util.List;

/**
 * @author YiHui
 * @date 2023/1/12
 */
@Data
@ConfigurationProperties(prefix = "net")//将外部配置文件中的属性注入到Java对象中
public class ProxyProperties {
    private List<ProxyType> proxy;

    @Data
    @Accessors(chain = true)//启用链式调用
    public static class ProxyType {
        /**
         * 代理类型
         */
        private Proxy.Type type;
        /**
         * 代理ip
         */
        private String ip;
        /**
         * 代理端口
         */
        private Integer port;
    }
}
