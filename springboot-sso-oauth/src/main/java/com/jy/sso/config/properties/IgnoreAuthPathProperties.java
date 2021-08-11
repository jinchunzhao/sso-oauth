package com.jy.sso.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 跳过认证路径
 *
 * @author JinChunZhao
 * @version 1.0
 * @date 2021-08-08 14:02
 */
@Data
@Component
@ConfigurationProperties(prefix = "oauth")
public class IgnoreAuthPathProperties {

    /**
     * 忽略认证的路径
     */
    private List<String> ignoreAuthUrls;
}
