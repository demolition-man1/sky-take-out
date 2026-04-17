package com.sky.utils;

import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;
@Component
@Slf4j
public class FileUploadUtil {
    @Autowired
    private AliOssUtil aliOssUtil;
    public String uploadFile(MultipartFile file) {
        try {
            String originalFileName = file.getOriginalFilename();
            if (originalFileName == null || originalFileName.isEmpty()) {
                throw new RuntimeException("文件名不能为空");
            }

            // 获取文件扩展名
            String extension = originalFileName.substring(originalFileName.lastIndexOf("."));

            // 生成唯一文件名，避免重名
            String objectName = UUID.randomUUID().toString() + extension;

            // 上传到OSS
            String url = aliOssUtil.upload(file.getBytes(), objectName);

            log.info("文件上传成功：{}", url);
            return url;
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败", e);
        }
    }
}

