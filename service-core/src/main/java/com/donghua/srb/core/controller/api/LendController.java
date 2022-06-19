package com.donghua.srb.core.controller.api;


import com.baomidou.mybatisplus.extension.api.R;
import com.donghua.common.result.Result;
import com.donghua.srb.base.util.JwtUtils;
import com.donghua.srb.core.pojo.entity.Lend;
import com.donghua.srb.core.service.LendService;
import com.donghua.srb.core.service.UserAccountService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 标的准备表 前端控制器
 * </p>
 *
 * @author zfk
 * @since 2022-05-29
 */
@Api(tags = "标的")
@RestController
@RequestMapping("/api/core/lend")
@Slf4j
public class LendController {

    @Resource
    private LendService lendService;

    @Resource
    private UserAccountService userAccountService;

    @ApiOperation("获取标的列表")
    @GetMapping("/list")
    public Result list() {
        List<Lend> lendList = lendService.selectList();
        return Result.ok().data("lendList", lendList);
    }

    @ApiOperation("获取标的信息")
    @GetMapping("/show/{id}")
    public Result show(
            @ApiParam(value = "标的id", required = true)
            @PathVariable Long id) {
        Map<String, Object> lendDetail = lendService.getLendDetail(id);
        return Result.ok().data("lendDetail", lendDetail);
    }

    @ApiOperation("查询账户余额")
    @GetMapping("/auth/getAccount")
    public Result getAccount(HttpServletRequest request){
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        BigDecimal account = userAccountService.getAccount(userId);
        return Result.ok().data("account", account);
    }

    @ApiOperation("计算投资收益")
    @GetMapping("/getInterestCount/{invest}/{yearRate}/{totalmonth}/{returnMethod}")
    public Result getInterestCount(
            @ApiParam(value = "投资金额", required = true)
            @PathVariable("invest") BigDecimal invest,

            @ApiParam(value = "年化收益", required = true)
            @PathVariable("yearRate")BigDecimal yearRate,

            @ApiParam(value = "期数", required = true)
            @PathVariable("totalmonth")Integer totalmonth,

            @ApiParam(value = "还款方式", required = true)
            @PathVariable("returnMethod")Integer returnMethod) {

        BigDecimal  interestCount = lendService.getInterestCount(invest, yearRate, totalmonth, returnMethod);
        return Result.ok().data("interestCount", interestCount);
    }

}
