package com.jy.sso.pojo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * sso 令牌信息
 *
 * @author JinChunZhao
 * @version 1.0
 * @date 2021-08-08 10:13
 */
@Data
public class SsoOauthToken implements Serializable {

    @ApiModelProperty(value = "令牌信息")

    private String accessToken;

    @ApiModelProperty(value = "刷新token(refresh_token)")

    private String refreshToken;

    @ApiModelProperty(value = "jwt短令牌")
    private String jti;

}