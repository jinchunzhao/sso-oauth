package com.jy.sso.service;

import com.jy.sso.pojo.LoginUser;
import com.jy.sso.pojo.SsoOauthToken;
import com.jy.sso.web.ResultBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * sso service
 *
 * @author JinChunZhao
 * @version 1.0
 * @date 2021-08-08 10:06
 */
public interface SsoOauthService {

    /**
     * 登录
     *
     * @param loginUser
     *        登录参数
     * @return
     *        结果信息
     */
    SsoOauthToken doLogin(LoginUser loginUser);

    /**
     * 刷新令牌
     *
     * @param refreshToken
     *        刷新令牌
     * @return
     *        结果信息
     */
    SsoOauthToken refreshToken(String refreshToken);

    /**
     * 退出登陆
     *
     * @param jti
     *        jwt唯一标识
     * @return
     *        结果信息
     */
    ResultBean doLogout(String jti, HttpServletRequest request, HttpServletResponse response);
}
