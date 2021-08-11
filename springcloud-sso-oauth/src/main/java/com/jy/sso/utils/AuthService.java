package com.jy.sso.utils;

import com.jy.sso.constants.SsoConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

@Service
public class AuthService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 判断cookie中jti是否存在
     * @param request
     * @return
     */
    public String getJtiFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if(Objects.nonNull(cookies) && cookies.length > 0){

            for (int i = 0; i < cookies.length; i++) {
                Cookie cookie = cookies[i];
                String cookieName = cookie.getName();
                if (Objects.equals(cookieName,"uid")){
                    return cookie.getValue();
                }
            }
        }

//        HttpCookie cookie = request.getCookies().getFirst("uid");
//        if (cookie!=null){
//            return cookie.getValue();
//        }
        return null;
    }

    /**
     * 判断redis中令牌是否过期
     * @param jti
     * @return
     */
    public String getTokenFromRedis(String jti) {
        String key = SsoConstant.SSO + jti;
        String token = stringRedisTemplate.boundValueOps(key).get();
        return token;
    }
}