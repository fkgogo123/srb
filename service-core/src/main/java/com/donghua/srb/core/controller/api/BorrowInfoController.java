package com.donghua.srb.core.controller.api;


import com.donghua.common.result.Result;
import com.donghua.srb.base.util.JwtUtils;
import com.donghua.srb.core.pojo.entity.BorrowInfo;
import com.donghua.srb.core.service.BorrowInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;

/**
 * <p>
 * 借款信息表 前端控制器
 * </p>
 *
 * @author zfk
 * @since 2022-05-29
 */
@Api(tags = "借款信息")
@RestController
@RequestMapping("/api/core/borrowInfo")
@Slf4j
public class BorrowInfoController {

    @Resource
    private BorrowInfoService borrowInfoService;

    @ApiOperation("获取借款额度")
    @GetMapping("/auth/getBorrowAmount")
    public Result getBorrowAmount(HttpServletRequest request) {
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        BigDecimal borrowAmount = borrowInfoService.getBorrowAmount(userId);
        return Result.ok().data("borrowAmount", borrowAmount);
    }

    @ApiOperation("提交借款申请")
    @PostMapping("/auth/save")
    public Result save(@RequestBody BorrowInfo borrowInfo, HttpServletRequest request) {

        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        borrowInfoService.saveBorrowInfo(borrowInfo, userId);
        return Result.ok().message("提交成功");
    }

    @ApiOperation("获取借款申请审批状态")
    @GetMapping("/auth/getBorrowInfoStatus")
    public Result getBorrowerStatus(HttpServletRequest request){
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        Integer status = borrowInfoService.getStatusByUserId(userId);
        return Result.ok().data("borrowInfoStatus", status);
    }
}

