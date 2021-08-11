package com.jy.sso.exception;

import com.jy.sso.web.ResultBeanCode;

import lombok.extern.slf4j.Slf4j;

/**
 * 异常抛出类
 *
 * @author jinchunzhao
 * @version 1.0
 * @date 2021-07-31 15:01
 */
@Slf4j
public class CastException {

    public static void cast(ResultBeanCode resultBeanCode){
        log.error("出现异常：{}",resultBeanCode.toString());
        throw new MyException(resultBeanCode);
    }

    public static void cast(String msg){
        log.error("出现异常：{}",msg);
        throw new MyException(msg);
    }

    public static void cast(String code,String msg){
        log.error("出现异常：{}",msg);
        throw new MyException(code,msg);
    }

}
