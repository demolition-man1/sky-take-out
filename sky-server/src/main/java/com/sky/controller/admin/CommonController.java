package com.sky.controller.admin;

import com.sky.properties.AliOssProperties;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import com.sky.utils.FileUploadUtil;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/admin/common")
@Api(tags = "通用接口")
@Slf4j
public class CommonController {
    @Autowired
    private AliOssUtil aliOssUtil; // Autowired by Spring
    @Autowired
    private FileUploadUtil fileUploadUtil;
    private final AliOssProperties aliOssProperties;

    public CommonController(AliOssProperties aliOssProperties) {
        this.aliOssProperties = aliOssProperties;
    }

    @PostMapping("/upload")
     public Result<String> upload(MultipartFile file) {
        log.info("文件上传：{}", file);
        //TODO 文件上传
        String fileName = fileUploadUtil.uploadFile(file);
        return Result.success(fileName);
    }
}
