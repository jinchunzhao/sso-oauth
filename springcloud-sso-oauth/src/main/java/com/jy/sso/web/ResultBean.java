package com.jy.sso.web;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 响应实体
 *
 * @author jinchunzhao
 * @version 1.0
 * @date 2021-07-31 10:48
 */
@Data
public class ResultBean<T> implements Serializable {

    private static final long serialVersionUID = 4935268037874834761L;
    @ApiModelProperty(value = "编码：200, 404，500")
    private String code;

    @ApiModelProperty(value = "消息")
    private String msg;

    @ApiModelProperty(value = "时间戳")
    private Date timestamp;

    @ApiModelProperty(value = "数据")
    private T data;

    public ResultBean() {
        this.timestamp = new Date();
    }

    public ResultBean( String code, String msg, T data) {
        this.timestamp = new Date();
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public ResultBean(String code, String msg) {
        this.timestamp = new Date();
        this.code = code;
        this.msg = msg;
    }


    public static <T> ResultBean<T> success() {

        return new ResultBean(ResultBeanCode.SUCCESS);
    }

    public static <T> ResultBean<T> success(T data) {

        return build(ResultBeanCode.SUCCESS, data);
    }

    public static <T> ResultBean<T> success(ResultBeanCode resultCode, T data) {

        return build(resultCode, data);
    }

    public static <T> ResultBean<T> failed() {

        return new ResultBean(ResultBeanCode.FAIL);
    }

    public static <T> ResultBean<T> failed(String msg) {

        return new ResultBean(ResultBeanCode.FAIL.getCode(),msg);
    }

    public static <T> ResultBean<T> failed(String code,String msg) {

        return new ResultBean(code,msg);
    }

    public static <T> ResultBean<T> failed(T data) {

        return build(ResultBeanCode.FAIL, data);
    }

    public static <T> ResultBean<T> failed(ResultBeanCode resultBeanCode, T data) {

        return build(resultBeanCode, data);
    }

    public static <T> ResultBean<T> build(ResultBeanCode resultBeanCode, T data) {
        return new ResultBean(resultBeanCode, data);
    }


    public ResultBean(ResultBeanCode resultBeanCode,T data) {
        this(resultBeanCode.getCode(), resultBeanCode.getMessage(),data);
    }

    public ResultBean(ResultBeanCode resultBeanCode) {
        this(resultBeanCode.getCode(), resultBeanCode.getMessage());
    }

}
