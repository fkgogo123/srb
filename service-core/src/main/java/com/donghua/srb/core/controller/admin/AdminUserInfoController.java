package com.donghua.srb.core.controller.admin;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.donghua.common.result.Result;
import com.donghua.srb.core.pojo.entity.UserInfo;
import com.donghua.srb.core.pojo.query.UserInfoQuery;
import com.donghua.srb.core.service.UserInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 * 用户基本信息 前端控制器
 * </p>
 *
 * @author zfk
 * @since 2022-05-29
 */
@Api(tags = "会员管理")
@RestController
@RequestMapping("/admin/core/userInfo")
@Slf4j
// @CrossOrigin
public class AdminUserInfoController {


    @Resource
    private UserInfoService userInfoService;

    @ApiOperation("获取会员分页列表")
    @GetMapping("/list/{page}/{limit}")
    public Result listPage(
            @ApiParam(value = "当前页码", required = true)
            @PathVariable("page") Long page,
            @ApiParam(value = "每页记录数", required = true)
            @PathVariable("limit") Long limit,
            @ApiParam(value = "查询对象", required = false) // 可以不传，显示默认列表
                    UserInfoQuery userInfoQuery) {

        Page<UserInfo> pageParam = new Page<>(page, limit);
        IPage<UserInfo> pageModel = userInfoService.listPage(pageParam, userInfoQuery);
        return Result.ok().data("pageModel", pageModel);
    }

    @ApiOperation("用户锁定和解锁")
    @PutMapping("/lock/{id}/{status}")
    public Result lock(
            @ApiParam(value = "用户id", required = true)
            @PathVariable("id") Long id,
            @ApiParam(value = "锁定状态，（0：锁定，1：正常）", required = true)
            @PathVariable("status") Integer status){
        userInfoService.lock(id, status);
        return Result.ok().message(status == 1 ? "解锁成功" : "锁定成功");
    }



}
