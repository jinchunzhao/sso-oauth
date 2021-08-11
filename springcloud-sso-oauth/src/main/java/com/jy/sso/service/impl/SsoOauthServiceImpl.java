package com.jy.sso.service.impl;

import com.jy.sso.config.properties.SsoOauthProperties;
import com.jy.sso.constants.SsoConstant;
import com.jy.sso.enums.GrantTypeEnum;
import com.jy.sso.exception.CastException;
import com.jy.sso.pojo.LoginUser;
import com.jy.sso.pojo.SsoOauthToken;
import com.jy.sso.service.SsoOauthService;
import com.jy.sso.web.ResultBean;
import com.jy.sso.web.ResultBeanCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * sso service 实现类
 *
 * @author JinChunZhao
 * @version 1.0
 * @date 2021-08-08 10:06
 */
@Slf4j
@Service
public class SsoOauthServiceImpl implements SsoOauthService {

    @Value("${spring.application.name}")
    private String serverName;

    @Autowired
    private SsoOauthProperties ssoOauthProperties;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @Autowired
    private LoadBalancerClient loadBalancerClient;

    @Override
    public SsoOauthToken doLogin(LoginUser loginUser) {

        //此处需要校验用户输入的账号密码是否正确，正确了，进行下面授权

        //获取当前服务的url
//        ServiceInstance serviceInstance = loadBalancerClient.choose(serverName);
//        URI uri = serviceInstance.getUri();
//        String url = uri + ssoOauthProperties.getPath();

        String url = ssoOauthProperties.getUrl();


        HttpEntity<MultiValueMap<String, String>> requestEntity = buildAuthRequestEntity(loginUser);

        // 调用失败回调函数
        //指定 restTemplate当遇到400或401响应时候也不要抛出异常，也要正常返回值
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {

            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                //当响应的值为400或401时候也要正常响应，不要抛出异常
                if (response.getRawStatusCode() != HttpStatus.BAD_REQUEST.value() && response.getRawStatusCode() != HttpStatus.UNAUTHORIZED.value()) {
                    super.handleError(response);
                }
            }
        });
        // 申请授权令牌
        ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        Map map = responseEntity.getBody();
        checkApplyToken(map);

        // 封装结果数据
        SsoOauthToken authToken = new SsoOauthToken();
        authToken.setAccessToken((String) map.get(SsoConstant.ACCESS_TOKEN));
        authToken.setRefreshToken((String) map.get(SsoConstant.REFRESH_TOKEN));
        authToken.setJti((String) map.get(SsoConstant.JTI));

        // 将jti作为redis中的key,将jwt作为redis中的value进行数据的存放
        String key = SsoConstant.SSO + authToken.getJti();
        stringRedisTemplate.boundValueOps(key).set(authToken.getAccessToken(), ssoOauthProperties.getTtl(),
            TimeUnit.SECONDS);
        return authToken;
    }

    @Override
    public SsoOauthToken refreshToken(String refreshToken) {

        //获取当前服务的url
        ServiceInstance serviceInstance = loadBalancerClient.choose(serverName);
        URI uri = serviceInstance.getUri();
        String url = uri + ssoOauthProperties.getPath();

        // 请求body
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add(SsoConstant.GRANT_TYPE, GrantTypeEnum.REFRESH_TOKEN.getCode());
        body.add(SsoConstant.REFRESH_TOKEN, refreshToken);


        // 请求headers
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(SsoConstant.AUTH,
            this.getHttpBasic(ssoOauthProperties.getClientId(), ssoOauthProperties.getClientSecret()));
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);


        //指定 restTemplate当遇到400或401响应时候也不要抛出异常，也要正常返回值
        // 调用失败回调函数
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {

            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                //当响应的值为400或401时候也要正常响应，不要抛出异常
                if (response.getRawStatusCode() != HttpStatus.BAD_REQUEST.value() && response.getRawStatusCode() != HttpStatus.UNAUTHORIZED.value()) {
                    super.handleError(response);
                }
            }
        });
        // 申请授权令牌
        ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Map.class);
        Map map = responseEntity.getBody();

        checkApplyToken(map);

        // 封装结果数据
        SsoOauthToken authToken = new SsoOauthToken();
        authToken.setAccessToken((String) map.get(SsoConstant.ACCESS_TOKEN));
        authToken.setRefreshToken((String) map.get(SsoConstant.REFRESH_TOKEN));
        authToken.setJti((String) map.get(SsoConstant.JTI));

        // 将jti作为redis中的key,将jwt作为redis中的value进行数据的存放
        String key = SsoConstant.SSO + authToken.getJti();

        stringRedisTemplate.delete(key);

        stringRedisTemplate.boundValueOps(key).set(authToken.getAccessToken(), ssoOauthProperties.getTtl(),
            TimeUnit.SECONDS);
        return authToken;
    }

    @Override
    public ResultBean doLogout(String jti, HttpServletRequest request, HttpServletResponse response) {
        String key = SsoConstant.SSO + jti;
        stringRedisTemplate.delete(key);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        new SecurityContextLogoutHandler().logout(request, response, authentication);
        try {
            String referer = request.getHeader("referer");
            log.info("退出后的跳转链接:{}",referer);
            if(StringUtils.isNotBlank(referer)){
                response.sendRedirect(request.getHeader("referer"));
            }
        } catch (IOException e) {
            log.error("退出登陆失败",e);
            return ResultBean.failed(ResultBeanCode.LOGOUT_ERROR);
        }

        return ResultBean.success();
    }

    /**
     * http basic认证信息
     *
     * 进行Base64编码,并将编码后的认证数据放到头文件中
     *
     * @param clientId
     *            客户端id
     * @param clientSecret
     *            客户端密钥
     * @return 结果信息
     */
    private String getHttpBasic(String clientId, String clientSecret) {
        //将客户端id和客户端密码拼接，按“客户端id:客户端密码”
        String value = clientId + ":" + clientSecret;
        //进行base64编码
        byte[] encode = Base64Utils.encode(value.getBytes());
        return SsoConstant.BASIC + new String(encode);
    }

    /**
     * 校验令牌是否申请成功
     *
     * @param map
     *            参数
     */
    private void checkApplyToken(Map map) {
        if (Objects.isNull(map) || map.isEmpty()) {
            // 申请令牌失败
            CastException.cast(ResultBeanCode.APPLY_TOKEN_ERROR);
        }

        if (map.get(SsoConstant.ACCESS_TOKEN) == null || map.get(SsoConstant.REFRESH_TOKEN) == null
            || map.get(SsoConstant.JTI) == null) {
            // 申请令牌失败
            CastException.cast(ResultBeanCode.APPLY_TOKEN_ERROR);
        }
    }

    /**
     * 构建授权请求头、请求body
     *
     * @param loginUser
     *            登录参数
     * @return 请求参数
     */
    private HttpEntity<MultiValueMap<String, String>> buildAuthRequestEntity(LoginUser loginUser) {

        // 请求body 指定认证类型、账号、密码
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add(SsoConstant.GRANT_TYPE, GrantTypeEnum.PASSWORD.getCode());
        body.add(SsoConstant.USERNAME, loginUser.getAccount());
        body.add(SsoConstant.PASSWORD, loginUser.getPassword());

        // 请求headers
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add(SsoConstant.AUTH,
            this.getHttpBasic(ssoOauthProperties.getClientId(), ssoOauthProperties.getClientSecret()));
        return new HttpEntity<>(body, headers);
    }
}
