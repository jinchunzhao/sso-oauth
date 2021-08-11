package com.jy.sso.config.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

import com.jy.sso.config.properties.EncryptProperties;
import com.jy.sso.utils.JksUtils;

/**
 * 资源服务配置授权控制 激活方法上的PreAuthorize注解
 *
 * @author JinChunZhao
 * @version 1.0
 * @date 2021-08-08 9:44
 */
@Configuration
@EnableResourceServer
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

    @Autowired
    private JwtAccessTokenConverter jwtAccessTokenConverter;

    @Autowired
    private EncryptProperties encryptProperties;

    /**
     * Http安全配置，对每个到达系统的http请求链接进行校验
     *
     * @param http
     *            http安全配置
     * @throws Exception
     *             任何异常
     */
    @Override
    public void configure(HttpSecurity http) throws Exception {

        jwtAccessTokenConverter.setVerifierKey(JksUtils.getPubKey(encryptProperties.getPublicKey()));

        // // 所有请求必须认证通过
        // http.authorizeRequests()
        // // 下边的路径放行
        // // 配置地址放行
        // .antMatchers("/user/add").permitAll().anyRequest().
        // // 其他地址需要认证授权
        // authenticated();

        // 所有请求必须认证通过
        http.authorizeRequests().anyRequest().authenticated(); // 其他地址需要认证授权
    }
}