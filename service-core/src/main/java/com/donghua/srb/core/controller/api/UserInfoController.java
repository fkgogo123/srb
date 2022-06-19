package com.donghua.srb.core.controller.api;


import com.donghua.common.exception.Assert;
import com.donghua.common.result.ResponseEnum;
import com.donghua.common.result.Result;
import com.donghua.common.util.RegexValidateUtils;
import com.donghua.srb.base.util.JwtUtils;
import com.donghua.srb.core.pojo.vo.LoginVO;
import com.donghua.srb.core.pojo.vo.RegisterVO;
import com.donghua.srb.core.pojo.vo.UserIndexVO;
import com.donghua.srb.core.pojo.vo.UserInfoVO;
import com.donghua.srb.core.service.UserInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 用户基本信息 前端控制器
 * </p>
 *
 * @author zfk
 * @since 2022-05-29
 */
@Api(tags = "会员接口")
@RestController
@RequestMapping("/api/core/userInfo")
@Slf4j
// @CrossOrigin
public class UserInfoController {

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private UserInfoService userInfoService;

    @ApiOperation("会员注册")
    @PostMapping("/register")
    public Result register(@RequestBody RegisterVO registerVO) {

        String mobile = registerVO.getMobile();
        String code = registerVO.getCode();
        String password = registerVO.getPassword();

        Assert.notEmpty(mobile, ResponseEnum.MOBILE_NULL_ERROR);
        Assert.isTrue(RegexValidateUtils.checkCellphone(mobile), ResponseEnum.MOBILE_ERROR);
        Assert.notEmpty(code, ResponseEnum.CODE_NULL_ERROR);
        Assert.notEmpty(password, ResponseEnum.PASSWORD_NULL_ERROR);

        // 校验验证码是否正确
        String codeGen = (String)redisTemplate.opsForValue().get("srb:sms:code:" + mobile);
        Assert.equals(code, codeGen, ResponseEnum.CODE_ERROR);

        // 注册
        userInfoService.register(registerVO);

        // 返回结果
        return Result.ok().message("注册成功");
    }

    @ApiOperation("会员登录")
    @PostMapping("/login")
    public Result login(@RequestBody LoginVO loginVO, HttpServletRequest httpServletRequest) {
        String mobile = loginVO.getMobile();
        String password = loginVO.getPassword();

        Assert.notEmpty(mobile, ResponseEnum.MOBILE_NULL_ERROR);
        Assert.isTrue(RegexValidateUtils.checkCellphone(mobile), ResponseEnum.MOBILE_ERROR);
        Assert.notEmpty(password, ResponseEnum.PASSWORD_NULL_ERROR);

        String ip = httpServletRequest.getRemoteAddr();
        UserInfoVO userInfoVO = userInfoService.login(loginVO, ip);

        return Result.ok().message("登录成功").data("userInfo", userInfoVO);
    }

    @ApiOperation("校验令牌")
    @GetMapping("/checkToken")
    public Result checkToken(HttpServletRequest httpServletRequest) {
        String token = httpServletRequest.getHeader("token");
        boolean b = JwtUtils.checkToken(token);
        if (b) {
            return Result.ok();
        } else {
            return Result.setResult(ResponseEnum.LOGIN_AUTH_ERROR);
        }
    }

    @ApiOperation("校验手机号是否注册")
    @GetMapping("/checkMobile/{mobile}")
    public boolean checkMobile(
            @ApiParam(value = "手机号", required = true)
            @PathVariable String mobile){
        return userInfoService.checkMobile(mobile);
    }

    @ApiOperation("获取个人空间用户信息")
    @GetMapping("/auth/getIndexUserInfo")
    public Result getIndexUserInfo(HttpServletRequest request) {
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        UserIndexVO userIndexVO = userInfoService.getIndexUserInfo(userId);
        return Result.ok().data("userIndexVO", userIndexVO);
    }
}

