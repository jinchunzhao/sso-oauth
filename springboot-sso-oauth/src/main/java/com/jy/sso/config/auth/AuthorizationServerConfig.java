package com.jy.sso.config.auth;

import com.jy.sso.config.properties.EncryptProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
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

import javax.sql.DataSource;
import java.security.KeyPair;

/**
 * Oauth2认证服务器配置
 *
 * @author JinChunZhao
 * @version 1.0
 * @date 2021-08-09 11:06
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
    private EncryptProperties encryptProperties;

    @Autowired
    private CustomUserAuthenticationConverter customUserAuthenticationConverter;

    @Autowired
    private JwtAccessTokenConverter jwtAccessTokenConverter;

    /**
     * 授权服务器的安全配置
     *
     * @param security
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security.allowFormAuthenticationForClients()
            .passwordEncoder(new BCryptPasswordEncoder())
            .tokenKeyAccess("permitAll()")
            .checkTokenAccess("isAuthenticated()");
    }

    /**
     * 客户端信息配置
     * @param clients
     * @throws Exception
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.jdbc(dataSource).clients(clientDetails());
    }

    /**
     * 授权服务器端点配置
     * @param endpoints
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {

        endpoints.accessTokenConverter(jwtAccessTokenConverter(customUserAuthenticationConverter))
            //认证管理器
            .authenticationManager(authenticationManager)
            //令牌存储
            .tokenStore(jwtTokenStore())
            //用户信息service
            .userDetailsService(userDetailsService);
    }

    /**
     * 客户端配置
     * @return
     */
    @Bean
    public ClientDetailsService clientDetails() {
        return new JdbcClientDetailsService(dataSource);
    }

    /*@Primary
    @Bean
    public DefaultTokenServices defaultTokenServices() {
        DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
        defaultTokenServices.setTokenStore(jwtTokenStore());
        defaultTokenServices.setSupportRefreshToken(true);
        return defaultTokenServices;
    }*/

    @Bean
    public JwtTokenStore jwtTokenStore() {
        return new JwtTokenStore(jwtAccessTokenConverter(customUserAuthenticationConverter));
    }


//    //读取密钥的配置
//    @Bean("keyProp")
//    public KeyStore keyProperties() throws Exception{
////        return new KeyProperties();
//        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
//        return keyStore;
//    }
//
//    @Resource(name = "keyProp")
//    private KeyStore keyStore;


    /**
     * JWT令牌转换器
     *
     * @return
     *        JWT令牌转换器
     */
    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter(CustomUserAuthenticationConverter customUserAuthenticationConverter){

        JwtAccessTokenConverter jwtAccessTokenConverter = new JwtAccessTokenConverter();

        ClassPathResource classPathResource = new ClassPathResource(encryptProperties.getLocation());
        //证书路径 changgou.jks
        KeyStoreKeyFactory keyStoreKeyFactory =
            new KeyStoreKeyFactory(classPathResource, encryptProperties.getSecret().toCharArray());

        KeyPair keyPair =
            keyStoreKeyFactory.getKeyPair(encryptProperties.getAlias(), encryptProperties.getPassword().toCharArray());

        jwtAccessTokenConverter.setKeyPair(keyPair);
        //配置自定义的CustomUserAuthenticationConverter
        DefaultAccessTokenConverter accessTokenConverter = (DefaultAccessTokenConverter) jwtAccessTokenConverter.getAccessTokenConverter();
        accessTokenConverter.setUserTokenConverter(customUserAuthenticationConverter);

        return jwtAccessTokenConverter;
    }


}
