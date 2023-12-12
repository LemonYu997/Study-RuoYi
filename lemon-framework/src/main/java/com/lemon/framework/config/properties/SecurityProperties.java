package com.lemon.framework.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * security 请求排除路径的属性配置
 * 详见 application.yml中的 security 配置项
 */
@Data
@Component
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {
    /**
     * 排除路径 详见application.yml中的 security.excludes 配置项
     */
    private String[] excludes;
}
