package com.jy.sso.config.auth;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.stereotype.Component;

import com.jy.sso.pojo.UserAuthJwt;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 加载用户信息
 *
 * @author JinChunZhao
 * @version 1.0
 * @date 2021-08-08 9:20
 */
@Slf4j
@Component
public class AuthUserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private ClientDetailsService clientDetailsService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 取出身份，如果身份为空说明没有认证
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 没有认证统一采用httpbasic认证，httpbasic中存储了client_id和client_secret，开始认证client_id和client_secret
        if (authentication == null) {
            ClientDetails clientDetails = clientDetailsService.loadClientByClientId(username);
            if (clientDetails != null) {
                // 秘钥
                String clientSecret = clientDetails.getClientSecret();
                // 静态方式
                // return new User(username,new BCryptPasswordEncoder().encode(clientSecret),
                // AuthorityUtils.commaSeparatedStringToAuthorityList(""));
                // 数据库查找方式
                return new UserAuthJwt(username, clientSecret,
                    AuthorityUtils.commaSeparatedStringToAuthorityList("admin,user"));
            }
        }

        if (StringUtils.isBlank(username)) {
            return null;
        }

        // 此处根据用户名查询用户信息
        String pwd = new BCryptPasswordEncoder().encode("admin");

        // 此处获取用户的角色
        String permissions = "admin,user";
        UserAuthJwt userAuthJwt =
            new UserAuthJwt(username, pwd, AuthorityUtils.commaSeparatedStringToAuthorityList(permissions));

        log.info("登录成功！用户: {}", JSONUtil.toJsonStr(userAuthJwt));

        return userAuthJwt;
    }
}
