package com.jy.sso.config.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.bootstrap.encrypt.KeyProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.security.KeyPair;

/**
 * Oauth2认证服务器配置
 *
 * @author JinChunZhao
 * @version 1.0
 * @date 2021-08-08 11:06
 */
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    @Qualifier("authUserDetailsServiceImpl")
    private UserDetailsService userDetailsService;

    @Autowired
    private CustomUserAuthenticationConverter customUserAuthenticationConverter;

    @Resource(name = "keyProp")
    private KeyProperties keyProperties;

    /**
     * 授权服务器的安全配置
     *
     * @param security
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security.allowFormAuthenticationForClients().passwordEncoder(new BCryptPasswordEncoder())
            .tokenKeyAccess("permitAll()").checkTokenAccess("isAuthenticated()");
    }

    /**
     * 客户端信息配置
     *
     * @param clients
     * @throws Exception
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.jdbc(dataSource).clients(clientDetails());
    }

    /**
     * 授权服务器端点配置
     *
     * @param endpoints
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {

        endpoints.accessTokenConverter(jwtAccessTokenConverter(customUserAuthenticationConverter))
            // 认证管理器
            .authenticationManager(authenticationManager)
            // 令牌存储
            .tokenStore(jwtTokenStore())
            // 用户信息service
            .userDetailsService(userDetailsService);
    }

    /**
     * 客户端配置
     *
     * @return
     */
    @Bean
    public ClientDetailsService clientDetails() {
        return new JdbcClientDetailsService(dataSource);
    }

    @Bean
    public JwtTokenStore jwtTokenStore() {
        return new JwtTokenStore(jwtAccessTokenConverter(customUserAuthenticationConverter));
    }

    /**
     * 读取密钥的配置
     *
     * @return 证书信息
     */
    @Bean("keyProp")
    public KeyProperties keyProperties() {
        return new KeyProperties();
    }

    /**
     * JWT令牌转换器
     *
     * @return JWT令牌转换器
     */
    @Bean
    public JwtAccessTokenConverter
        jwtAccessTokenConverter(CustomUserAuthenticationConverter customUserAuthenticationConverter) {

        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        // 证书路径、证书秘钥
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(keyProperties.getKeyStore().getLocation(),
            keyProperties.getKeyStore().getSecret().toCharArray());
        // 证书别名、证书访问密码
        KeyPair keyPair = keyStoreKeyFactory.getKeyPair(keyProperties.getKeyStore().getAlias(),
            keyProperties.getKeyStore().getPassword().toCharArray());
        converter.setKeyPair(keyPair);
        // 配置自定义的CustomUserAuthenticationConverter
        DefaultAccessTokenConverter accessTokenConverter =
            (DefaultAccessTokenConverter) converter.getAccessTokenConverter();
        accessTokenConverter.setUserTokenConverter(customUserAuthenticationConverter);
        return converter;
    }

}
