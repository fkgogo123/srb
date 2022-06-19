package com.donghua.srb.sms.controller.controller;

import com.donghua.common.exception.Assert;
import com.donghua.common.result.ResponseEnum;
import com.donghua.common.result.Result;
import com.donghua.common.util.RandomUtils;
import com.donghua.common.util.RegexValidateUtils;
import com.donghua.srb.sms.client.CoreUserInfoClient;
import com.donghua.srb.sms.service.SmsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/sms")
@Api(tags = "短信管理")
// @CrossOrigin // 跨域
@Slf4j
public class ApiSmsController {

    @Resource
    private SmsService smsService;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private CoreUserInfoClient coreUserInfoClient;


    @ApiOperation("发送验证码")
    @GetMapping("/send/{mobile}")
    public Result send(
            @ApiParam(value = "手机号", required = true)
            @PathVariable String mobile) {

        // 校验手机号不能为空
        Assert.notEmpty(mobile, ResponseEnum.MOBILE_NULL_ERROR);
        // 校验手机号是否合法
        Assert.isTrue(RegexValidateUtils.checkCellphone(mobile), ResponseEnum.MOBILE_ERROR);

        // 判断手机号是够已经注册，需要调用service_core的服务，判断手机号是否已经注册。
        // 远程调用
        boolean b = coreUserInfoClient.checkMobile(mobile);
        log.info("coreUserInfoClient.checkMobile()远程调用结果：" + b);
        Assert.isTrue(!b, ResponseEnum.MOBILE_EXIST_ERROR);

        String code = RandomUtils.getFourBitRandom();
        HashMap<String, Object> map = new HashMap<>();
        map.put("code", code);

        // 调式的过程，暂时屏蔽掉，直接从Redis中取出要发送的验证码
        // smsService.send(mobile, SmsProperties.TEMPLATE_CODE, map);

        // 把验证码存入Redis
        redisTemplate.opsForValue().set("srb:sms:code:" + mobile, code, 1, TimeUnit.MINUTES);
        System.out.println("已发送验证码：" + code);
        return Result.ok().message("短信发送成功：" + code);
    }
}
