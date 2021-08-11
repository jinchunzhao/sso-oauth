package com.jy.sso.config.auth;

import com.jy.sso.pojo.UserAuthJwt;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.provider.token.DefaultUserAuthenticationConverter;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 自定义 JWT令牌转换器
 *
 * @author JinChunZhao
 * @version 1.0
 * @date 2021-08-08 16:32
 */
@Component
public class CustomUserAuthenticationConverter extends DefaultUserAuthenticationConverter {

    @Resource(name = "authUserDetailsServiceImpl")
    private UserDetailsService userDetailsService;

    @Override
    public Map<String, ?> convertUserAuthentication(Authentication authentication) {
        LinkedHashMap response = new LinkedHashMap();
        String name = authentication.getName();
        response.put("username", name);

        Object principal = authentication.getPrincipal();
        UserAuthJwt userJwt = null;
        if (principal instanceof UserAuthJwt) {
            userJwt = (UserAuthJwt) principal;
        } else {
            // refresh_token默认不去调用userdetailService获取用户信息，这里我们手动去调用，得到 UserJwt
            UserDetails userDetails = userDetailsService.loadUserByUsername(name);
            userJwt = (UserAuthJwt) userDetails;
        }
        response.put("name", userJwt.getUsername());
        response.put("id", userJwt.getId());
        if (authentication.getAuthorities() != null && !authentication.getAuthorities().isEmpty()) {
            response.put("authorities", AuthorityUtils.authorityListToSet(authentication.getAuthorities()));
        }
        return response;
    }

}
