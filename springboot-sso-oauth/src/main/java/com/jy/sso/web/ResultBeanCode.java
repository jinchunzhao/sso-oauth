package com.jy.sso.web;

import lombok.Getter;

/**
 * 响应编码枚举类
 *
 * @author jinchunzhao
 * @version 1.0
 * @date 2021-07-31 11:24
 */
@Getter
public enum ResultBeanCode {

    SUCCESS("200", "成功"),

    FAIL("400", "失败"),

    ERROR("500", "系统错误"),

    LOGIN_ERROR("501", "账号或密码错误"),

    APPLY_TOKEN_ERROR("502", "申请令牌失败"),

    REFRESH_TOKEN_ERROR("503","刷新令牌失败"),

    LOGOUT_ERROR("504","用户退出失败"),

    UNAUTHORIZED("401", "无权限访问"),

    NOT_FOUND("404", "路径不存在");

    String code;

    String message;

    ResultBeanCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String toString() {
        return "ResultBeanCode{" + "code='" + code + '\'' + ", message='" + message + '\'' + '}';
    }
}
