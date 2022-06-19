package com.donghua.srb.core.controller.admin;


import com.donghua.common.result.Result;
import com.donghua.srb.core.pojo.entity.UserLoginRecord;
import com.donghua.srb.core.service.UserLoginRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 用户登录记录表 前端控制器
 * </p>
 *
 * @author zfk
 * @since 2022-05-29
 */
@Api(tags = "会员登录日志接口")
@RestController
@RequestMapping("/admin/core/userLoginRecord")
@Slf4j
// @CrossOrigin
public class AdminUserLoginRecordController {

    @Resource
    private UserLoginRecordService userLoginRecordService;

    @ApiOperation("获取会员登录日志")
    @GetMapping("/listTop50/{userId}")
    public Result listTop50(
            @ApiParam(value = "用户id", required = true)
            @PathVariable("userId") Integer userId){
        List<UserLoginRecord> userLoginRecords = userLoginRecordService.listTop50(userId);
        return Result.ok().data("list", userLoginRecords);
    }
}

