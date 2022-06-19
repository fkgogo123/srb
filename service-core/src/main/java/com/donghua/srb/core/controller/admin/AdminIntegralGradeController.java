package com.donghua.srb.core.controller.admin;


import com.donghua.common.result.Result;
import com.donghua.srb.core.pojo.entity.IntegralGrade;
import com.donghua.srb.core.service.IntegralGradeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 积分等级表 前端控制器
 * </p>
 *
 * @author zfk
 * @since 2022-05-29
 */
@Api(tags = "积分等级管理")
// @CrossOrigin
@RestController
@RequestMapping("/admin/core/integralGrade")
@Slf4j
public class AdminIntegralGradeController {

    @Resource
    private IntegralGradeService integralGradeService;

    @ApiOperation("积分等级列表")
    @GetMapping("/list")
    public Result listAll() {

        log.info("打印所有的积分等级信息");
        log.info("/admin/core/integralGrade/list");

        List<IntegralGrade> list = integralGradeService.list();
        return Result.ok().data("list",list).message("获取结果成功");
    }

    @ApiOperation(value = "根据id删除数据记录", notes = "逻辑删除数据记录")
    @DeleteMapping("/remove/{id}")
    public Result removeById(
            @ApiParam(value = "数据id", example = "100", required = true)
            @PathVariable Long id) {
        boolean b = integralGradeService.removeById(id);
        if (b) {
            return Result.ok().message("删除成功");
        } else {
            return Result.error().message("删除失败");
        }
    }

    @ApiOperation("新增积分等级")
    @PostMapping("/save")
    public Result save(
            @ApiParam(value = "积分等级对象", required = true)
            @RequestBody IntegralGrade integralGrade) {
        boolean b = integralGradeService.save(integralGrade);
        if (b) {
            return Result.ok().message("新增成功");
        } else {
            return Result.error().message("新增失败");
        }
    }

    @ApiOperation("根据id获取积分等级")
    @GetMapping("/get/{id}")
    public Result getById(
            @ApiParam(value = "积分对象id", example = "1", required = true)
            @PathVariable Long id) {
        IntegralGrade integralGrade = integralGradeService.getById(id);
        if (integralGrade != null) {
            return Result.ok().data("record", integralGrade);
        } else {
            return Result.error().message("数据不存在");
        }
    }

    @ApiOperation("根据id更新积分等级")
    @PutMapping("/update")
    public Result updateById(
            @ApiParam(value = "积分等级对象", required = true)
            @RequestBody IntegralGrade integralGrade) {
        boolean b = integralGradeService.updateById(integralGrade);
        if (b) {
            return Result.ok().message("更新成功");
        } else {
            return Result.error().message("更新失败");
        }
    }
}

