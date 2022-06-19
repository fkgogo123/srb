package com.donghua.srb.oss.controller;

import com.donghua.common.exception.BusinessException;
import com.donghua.common.result.ResponseEnum;
import com.donghua.common.result.Result;
import com.donghua.srb.oss.service.FileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;

@Api(tags = "阿里云文件管理")
// @CrossOrigin //跨域
@RestController
@RequestMapping("/api/oss/file")
public class FileController {

    @Resource
    private FileService fileService;

    @ApiOperation("上传文件")
    @PostMapping("/upload")
    public Result upload(
            @ApiParam(value = "文件", required = true)
            @RequestParam("file") MultipartFile file,
            @ApiParam(value = "模块", required = true)
            @RequestParam("module") String module
            ) {

        try {
            InputStream inputStream = file.getInputStream();
            String originalFilename = file.getOriginalFilename();
            String url = fileService.upload(inputStream, module, originalFilename);
            return Result.ok().message("文件上传成功").data("url", url);
        } catch (IOException e) {
            throw new BusinessException(ResponseEnum.UPLOAD_ERROR, e);
        }
    }

    @ApiOperation("删除oss文件")
    @DeleteMapping("/remove")
    public Result remove(
            @ApiParam(value = "要删除的文件", required = true)
            @RequestParam("url") String url) {

        fileService.remoeFile(url);
        return Result.ok().message("删除成功");

    }

}
