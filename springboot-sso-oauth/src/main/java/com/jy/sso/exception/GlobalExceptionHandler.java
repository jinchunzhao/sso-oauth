package com.jy.sso.exception;

import com.jy.sso.web.ResultBean;
import com.jy.sso.web.ResultBeanCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;

/**
 * 全局异常捕捉处理Handler
 *
 * @author JinChunZhao
 * @version 1.0
 * @date 2021-08-08 10:56
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 捕捉自定义C3Exception异常，并格式化输出
     *
     * @param ex
     *            异常
     * @param request
     *            http请求对象
     * @return 格式化返回值
     */
    @ExceptionHandler(MyException.class)
    public ResponseEntity<ResultBean> handleC3Exception(MyException ex, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("业务处理异常,请求路径:{}", requestURI, ex);

        return ResponseEntity
            .ok(ResultBean.failed(ex.getCode(), ex.getMsg()));
    }



    /**
     * 捕捉Exception异常，并格式化输出
     *
     * @param ex
     *            异常
     * @param request
     *            http请求对象
     * @return 格式化返回值
     */
    @ExceptionHandler(value = {Exception.class})
    @ResponseBody
    public ResultBean error(Exception ex, HttpServletRequest request) {
        log.error("未知运行时异常,请求路径:{}", request.getRequestURI(), ex);
        return ResultBean.failed(
            ResultBeanCode.ERROR);
    }

    /**
     * 捕捉NoHandlerFoundException异常，并格式化输出
     *
     * @param ex
     *            异常
     * @param request
     *            http请求对象
     * @return 格式化返回值
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ResponseBody
    public ResultBean handle404(NoHandlerFoundException ex, HttpServletRequest request) {
        log.error("请求路径异常,请求路径:{}", request.getRequestURI(), ex);
        return ResultBean.failed(
            ResultBeanCode.NOT_FOUND);
    }

    /**
     * 校验入参
     *
     * @param e
     *            异常
     * @param request
     *            请求
     * @return 结果信息
     */
    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    @ResponseBody
    public ResultBean methodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        log.error("校验异常,请求路径:{}", request.getRequestURI(), e);
        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        log.error("校验异常信息:{}", message);
        return ResultBean
            .failed(message);
    }

    /**
     * 捕捉ConstraintViolationException异常，并格式化输出
     *
     * @param ex
     *            异常
     * @param request
     *            http请求对象
     * @return 格式化返回值
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ResultBean validParam(ConstraintViolationException ex, HttpServletRequest request) {
        log.error("请求路径异常,请求路径:{}", request.getRequestURI(), ex);
        String message = ex.getMessage();
        String substring = message.substring(message.lastIndexOf(":"));
        return ResultBean
            .failed(substring);

    }



    /**
     * 捕捉BadSqlGrammarException异常，并格式化输出
     *
     * @param ex
     *            异常
     * @param request
     *            http请求对象
     * @return 格式化返回值
     */
    @ExceptionHandler(value = {BadSqlGrammarException.class})
    @ResponseBody
    public ResultBean badSqlError(BadSqlGrammarException ex, HttpServletRequest request) {
        log.error("BadSqlGrammarException,请求路径:{}", request.getRequestURI(), ex);
        return ResultBean.failed();
    }


}
