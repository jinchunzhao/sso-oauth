package com.jy.sso.config.filter;

import com.jy.sso.config.properties.IgnoreAuthPathProperties;
import com.jy.sso.config.request.SsoHttpServletRequest;
import com.jy.sso.constants.SsoConstant;
import com.jy.sso.exception.CastException;
import com.jy.sso.utils.AuthService;
import com.jy.sso.web.ResultBeanCode;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 安全认证过滤器
 *
 * @author JinChunZhao
 * @version 1.0
 * @date 2021-08-08 10:29
 */
@Component
public class AuthFilter implements Filter, Ordered {

    @Autowired
    private IgnoreAuthPathProperties ignoreAuthPathProperties;

    @Autowired
    private AuthService authService;

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
        throws IOException, ServletException {

        // 获取当前请求对象
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        HttpServletRequest request = (HttpServletRequest) servletRequest;

        String path = request.getRequestURI();
        List<String> ignoreAuthUrls = ignoreAuthPathProperties.getIgnoreAuthUrls();

        if (CollectionUtils.isNotEmpty(ignoreAuthUrls)) {
            for (int i = 0; i < ignoreAuthUrls.size(); i++) {
                String ignoreUrl = ignoreAuthUrls.get(i);
                if (FilenameUtils.wildcardMatch(path, ignoreUrl)) {
                    // 放行
                    chain.doFilter(servletRequest, servletResponse);
                    return;
                }
            }
        }

        // 判断cookie上是否存在jti
        String jti = authService.getJtiFromCookie(request);
        if (StringUtils.isEmpty(jti)) {
            CastException.cast(ResultBeanCode.UNAUTHORIZED);
        }

        // 判断redis中token是否存在
        String redisToken = authService.getTokenFromRedis(jti);
        if (StringUtils.isBlank(redisToken)) {
            CastException.cast(ResultBeanCode.UNAUTHORIZED);
        }

        // 校验通过 , 请求头增强，放行
        SsoHttpServletRequest ssoHttpServletRequest = new SsoHttpServletRequest(request);
        ssoHttpServletRequest.putHeader(SsoConstant.AUTH, SsoConstant.BEARER + redisToken);

        chain.doFilter(ssoHttpServletRequest, servletResponse);

    }

    @Override
    public void destroy() {

    }
}