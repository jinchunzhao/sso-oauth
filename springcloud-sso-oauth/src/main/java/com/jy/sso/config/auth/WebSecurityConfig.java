package com.jy.sso.config.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 认证服务WebSecurityConfig类
 *
 * @author JinChunZhao
 * @version 1.0
 * @date 2021-08-08 9:39
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    @Qualifier("authUserDetailsServiceImpl")
    private UserDetailsService userDetailsService;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    /**
     * 忽略安全拦截的URL
     *
     * @param web
     *            web过滤
     * @throws Exception
     *             任何异常
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        // web.ignoring().antMatchers("/assets/**", "/css/**", "/images/**");
        web.ignoring().antMatchers("/sso/oauth/login", "/sso/oauth/logout", "/sso/oauth/refresh/token");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
            // 启用Http基本身份验证
            .httpBasic().and()
            // 启用表单身份验证
            .formLogin().and()
            // 限制基于Request请求访问
            .authorizeRequests().anyRequest()
            // 其他请求都需要经过验证
            .authenticated();

        // http.formLogin()
        // .loginPage("/login")
        // .and()
        // .authorizeRequests()
        // .antMatchers("/login").permitAll()
        // .anyRequest()
        // .authenticated()
        // .and().csrf().disable().cors();
    }

    /**
     * 采用BCryptPasswordEncoder对密码进行编码
     *
     * @return 编码对象
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 创建授权管理认证对象
     *
     * @return 授权管理认证对象
     * @throws Exception
     *             任何异常
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
