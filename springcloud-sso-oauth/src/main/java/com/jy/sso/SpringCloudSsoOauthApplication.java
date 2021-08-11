package com.jy.sso;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * sso 启动类
 *
 * @author jinchunzhao
 * @version 1.0
 * @date 2021-08-08 19:56
 */
@SpringBootApplication
public class SpringCloudSsoOauthApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringCloudSsoOauthApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
}
