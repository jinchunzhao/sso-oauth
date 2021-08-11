package com.jy.sso.pojo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * 权限扩展用户实体类
 *
 * @author JinChunZhao
 * @version 1.0
 * @date 2021-08-08 9:18
 */
@Getter
@Setter
@ToString
public class UserAuthJwt extends User {

    private String tel;

    private Long id;


    public UserAuthJwt(String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
    }

    public UserAuthJwt(String username, String password, boolean enabled, boolean accountNonExpired,
        boolean credentialsNonExpired, boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
    }

    public UserAuthJwt(String username, String password, Collection<? extends GrantedAuthority> authorities,
        String tel) {
        super(username, password, authorities);
        this.tel = tel;
    }
}
