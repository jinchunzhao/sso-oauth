package com.jy.sso.pojo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 用户登录实体
 *
 * @author JinChunZhao
 * @version 1.0
 * @date 2021-08-08 9:59
 */
@Data
public class LoginUser implements Serializable {

    @NotBlank(message = "请输入账号")
    @ApiModelProperty(value = "账号")
    private String account;

    @NotBlank(message = "请输入密码")
    @ApiModelProperty(value = "密码")
    private String password;



}
