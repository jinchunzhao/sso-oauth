package com.jy.sso.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * sso权限配置类
 *
 * @author JinChunZhao
 * @version 1.0
 * @date 2021-08-08 10:23
 */
@Data
@Component
@ConfigurationProperties(prefix = "sso.auth")
public class SsoOauthProperties {

    private Long ttl;

    private String url;

    private String clientId;

    private String clientSecret;

    private String cookieDomain;

    private Integer cookieMaxAge;
}
