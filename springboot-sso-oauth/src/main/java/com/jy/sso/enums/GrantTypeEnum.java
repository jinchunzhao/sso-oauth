package com.jy.sso.enums;

import lombok.Getter;

/**
 * 授权模式枚举定义类
 *
 * @author JinChunZhao
 * @version 1.0
 * @date 2021-08-08 11:44
 */
@Getter
public enum GrantTypeEnum {

    PASSWORD("password", "密码模式"), AUTH_CODE("authorization_code", "授权码模式"), REFRESH_TOKEN("refresh_token", "刷新token"),
    CLIENT_CREDENTIALS("client_credentials", "客户端模式"), IMPLICIT("Implicit", "隐式授权模式");

    private final String code;

    private final String name;

    GrantTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
