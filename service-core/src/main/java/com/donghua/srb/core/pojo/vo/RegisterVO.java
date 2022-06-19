package com.donghua.srb.core.pojo.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * value object
 * 对一些前端使用的表单数据，用vo对象类进行实例化
 */
@Data
@ApiModel(description="注册对象")
public class RegisterVO {

    @ApiModelProperty(value = "用户类型")
    private Integer userType;

    @ApiModelProperty(value = "手机号")
    private String mobile;

    @ApiModelProperty(value = "验证码")
    private String code;

    @ApiModelProperty(value = "密码")
    private String password;
}