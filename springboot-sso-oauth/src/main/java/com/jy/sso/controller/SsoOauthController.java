package com.jy.sso.controller;

import com.jy.sso.config.properties.SsoOauthProperties;
import com.jy.sso.exception.CastException;
import com.jy.sso.pojo.LoginUser;
import com.jy.sso.pojo.SsoOauthToken;
import com.jy.sso.service.SsoOauthService;
import com.jy.sso.utils.CookieUtil;
import com.jy.sso.web.ResultBean;
import com.jy.sso.web.ResultBeanCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.token.ConsumerTokenServices;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * 单点登录接口
 *
 * @author JinChunZhao
 * @version 1.0
 * @date 2021-08-08 9:50
 */
@RestController
@RequestMapping("/sso/oauth")
@Api(value = "sso管理", tags = {"sso管理相关接口"})
public class SsoOauthController {

    @Autowired
    private SsoOauthProperties ssoOauthProperties;

    @Autowired
    private SsoOauthService ssoOauthService;

    @Resource
    private ConsumerTokenServices consumerTokenServices;

    @ApiOperation(value = "账号密码登录", notes = "账号密码登录")
    @ResponseBody
    @PostMapping("/login")
    public ResultBean<SsoOauthToken> doLogin(@RequestBody @Validated LoginUser loginUser, HttpServletResponse response){

        //申请令牌 authtoken
        SsoOauthToken ssoOauthToken = ssoOauthService.doLogin(loginUser);

        if (Objects.isNull(ssoOauthToken)){
            return ResultBean.failed(ResultBeanCode.REFRESH_TOKEN_ERROR,ssoOauthToken);
        }

        //将jti的值存入cookie中
        this.saveJtiToCookie(ssoOauthToken.getJti(),response);
        return ResultBean.success(ssoOauthToken);
    }

    @ApiOperation(value = "刷新令牌", notes = "刷新令牌")
    @ResponseBody
    @PostMapping("/refresh/token")
    public ResultBean<SsoOauthToken> refreshToken(String refreshToken, HttpServletResponse response){
        if (StringUtils.isBlank(refreshToken)){
            CastException.cast("刷新令牌不能为空");
        }
        SsoOauthToken ssoOauthToken = ssoOauthService.refreshToken(refreshToken);

        if (Objects.isNull(ssoOauthToken)){
            return ResultBean.failed(ResultBeanCode.LOGIN_ERROR,ssoOauthToken);
        }

        //将jti的值存入cookie中
        this.saveJtiToCookie(ssoOauthToken.getJti(),response);
        return ResultBean.success(ssoOauthToken);
    }


    @ApiOperation(value = "测试接口", notes = "测试接口")
    @ResponseBody
    @GetMapping("/test/list")
    public ResultBean testList(){
        return ResultBean.success("测试通过");
    }


    @ResponseBody
    @ApiOperation(value = "退出登录", notes = "退出登录")
    @PostMapping("/logout")
    public ResultBean doLogout(String jti, HttpServletRequest request,HttpServletResponse response){

        if (StringUtils.isBlank(jti)){
            return  ResultBean.failed("参数缺失");
        }


        return ssoOauthService.doLogout(jti,request,response);
    }



    /**
     * 将令牌的短标识jti存入到cookie中
     *
     * @param jti
     *        令牌的短标识
     * @param response
     *        响应流
     */
    private void saveJtiToCookie(String jti, HttpServletResponse response) {
        String cookieDomain = ssoOauthProperties.getCookieDomain();
        Integer cookieMaxAge = ssoOauthProperties.getCookieMaxAge();
        CookieUtil.addCookie(response,cookieDomain,"/","uid",jti,cookieMaxAge,false);
    }
}
