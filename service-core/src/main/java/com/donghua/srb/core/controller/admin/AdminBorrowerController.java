package com.donghua.srb.core.controller.admin;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.donghua.common.result.Result;
import com.donghua.srb.core.pojo.entity.Borrower;
import com.donghua.srb.core.pojo.vo.BorrowerApprovalVO;
import com.donghua.srb.core.pojo.vo.BorrowerDetailVO;
import com.donghua.srb.core.service.BorrowerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 * 借款人 前端控制器
 * </p>
 *
 * @author zfk
 * @since 2022-05-29
 */
@Api(tags = "借款人管理")
@RestController
@RequestMapping("/admin/core/borrower")
@Slf4j
public class AdminBorrowerController {

    @Resource
    private BorrowerService borrowerService;

    @ApiOperation("获取借款人分页列表")
    @GetMapping("/list/{page}/{limit}")
    public Result listPage(
            @ApiParam(value = "当前页码", required = true)
            @PathVariable Long page,

            @ApiParam(value = "每页记录数", required = true)
            @PathVariable Long limit,

            @ApiParam(value = "查询关键字", required = false)
            @RequestParam String keyword) {
        //这里的@RequestParam其实是可以省略的，但是在目前的swagger版本中（2.9.2）不能省略，
        //否则默认将没有注解的参数解析为body中的传递的数据

        Page<Borrower> pageParam = new Page<>(page, limit);
        IPage<Borrower> pageModel = borrowerService.listPage(pageParam, keyword);
        return Result.ok().data("pageModel", pageModel);
    }

    @ApiOperation("获取借款人详情信息")
    @GetMapping("/show/{id}")
    public Result show(
            @ApiParam(value = "借款人id", required = true)
            @PathVariable Long id){
        BorrowerDetailVO borrowerDetailVO = borrowerService.getBorrowerDetailVOById(id);
        return Result.ok().data("borrowerDetailVO", borrowerDetailVO);
    }

    @ApiOperation("借款额度审批")
    @PostMapping("/approval")
    public Result approval(@RequestBody BorrowerApprovalVO borrowerApprovalVO) {
        borrowerService.approval(borrowerApprovalVO);
        return Result.ok().message("审批完成");
    }
}

