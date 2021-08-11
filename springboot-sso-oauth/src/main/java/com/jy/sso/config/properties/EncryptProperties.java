package com.jy.sso.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 证书配置类
 *
 * @author jinchunzhao
 * @version 1.0
 * @date 2021-08-10 19:45
 */
@Data
@Component
@ConfigurationProperties(prefix = "encrypt.key-store")
public class EncryptProperties {

    /**
     * 证书路径
     */
    private  String location;

    /**
     * 密钥的访问密码 证书秘钥
     */
    private String secret;

    /**
     * 密钥的别名
     */
    private String alias;

    /**
     * 密钥库的访问密码 证书密码
     */
    private String password;

    /**
     * 公钥名称
     *
     */
    private String publicKey;

}
