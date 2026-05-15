package com.sky.controller.user;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URLEncoder;

@RestController("userCommonController")
@RequestMapping("/user/common")
@Api(tags = "C端-通用接口")
@Slf4j
public class CommonController {

    /**
     * 文件下载
     * @param fileName
     * @param response
     */
    @GetMapping("/download")
    @ApiOperation("文件下载")
    public void download(String fileName, HttpServletResponse response) {
        log.info("文件下载，文件名：{}", fileName);
        
        try {
            // 文件路径（这里需要根据实际情况配置）
            String filePath = "C:/uploads/" + fileName;
            File file = new File(filePath);
            
            if (!file.exists()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            // 设置响应头
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            
            // 读取文件并输出
            FileInputStream fis = new FileInputStream(file);
            OutputStream os = response.getOutputStream();
            
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            
            fis.close();
            os.flush();
            os.close();
        } catch (Exception e) {
            log.error("文件下载失败", e);
        }
    }
}
