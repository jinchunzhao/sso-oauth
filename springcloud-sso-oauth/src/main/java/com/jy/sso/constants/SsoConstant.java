package com.jy.sso.constants;

/**
 * sso 常量定义
 *
 * @author JinChunZhao
 * @version 1.0
 * @date 2021-08-08 14:09
 */
public interface SsoConstant {

    /**
     * 授权模式
     */
    String GRANT_TYPE = "grant_type";

    /**
     * 用户账号
     */
    String USERNAME = "username";

    /**
     * 用户密码
     */
    String PASSWORD = "password";

    /**
     * 请求头 key
     */
    String AUTH = "Authorization";

    /**
     * 令牌token
     */
    String ACCESS_TOKEN = "access_token";

    /**
     * 刷新令牌
     */
    String REFRESH_TOKEN = "refresh_token";

    /**
     * jwt唯一标识
     */
    String JTI = "jti";

    /**
     * redis hash存储key
     */
    String SSO = "sso:";

    /**
     * 客户端授权请求头前缀
     */
    String BASIC = "Basic ";

    /**
     * bearer认证参数
     */
    String BEARER = "Bearer ";
}
