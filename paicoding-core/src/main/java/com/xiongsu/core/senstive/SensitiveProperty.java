package com.xiongsu.core.senstive;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 敏感词相关配置，db配置表中的配置优先级更高，支持动态刷新
 *
 * @author YiHui
 * @date 2023/8/9
 */
@Data
@Component
@ConfigurationProperties(prefix = SensitiveProperty.SENSITIVE_KEY_PREFIX)//它可以自动读取配置文件中以 paicoding.sensitive 为前缀的配置信息，并将其绑定到类的字段中。
public class SensitiveProperty {//用于配置敏感词的校验机制。
    public static final String SENSITIVE_KEY_PREFIX = "paicoding.sensitive";
    /**
     * true 表示开启敏感词校验
     */
    private Boolean enable;

    /**
     * 自定义的敏感词
     */
    private List<String> deny;

    /**
     * 自定义的非敏感词
     */
    private List<String> allow;
}
