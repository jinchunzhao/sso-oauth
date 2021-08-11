package com.jy.sso.exception;

import com.jy.sso.web.ResultBeanCode;

import java.util.Date;

/**
 * 自定义异常
 *
 * @author jinchunzhao
 * @version 1.0
 * @date 2021-07-31 11:23
 */
public class MyException extends RuntimeException {

    private static final long serialVersionUID = 3549232418394213890L;

    private String code = "400";

    private String msg;

    private Date timestamp;

    public MyException(String code, String msg) {
        this.code = code;
        this.msg = msg;
        this.timestamp = new Date();
    }

    public MyException(ResultBeanCode resultBeanCode) {
        this.msg = resultBeanCode.getMessage();
        this.code = resultBeanCode.getCode();
        this.timestamp = new Date();
    }

    public MyException(String msg) {
        this.timestamp = new Date();
        this.msg = msg;
        this.code = ResultBeanCode.FAIL.getCode();
    }

    private MyException() {}



    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
