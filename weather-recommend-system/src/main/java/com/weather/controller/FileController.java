package com.weather.controller;

import com.weather.vo.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/files") // ✅ 修改1：改为复数，与前端保持一致
@CrossOrigin(origins = "*") // 允许跨域
public class FileController {

    @Value("${file.upload-path}")
    private String uploadPath;

    @Value("${server.port:8080}")
    private String port;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    /**
     * 文件上传接口
     */
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        // 1. 获取文件后缀
        String originalFilename = file.getOriginalFilename();
        String suffix = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        } else {
            suffix = ".jpg"; // 默认后缀
        }

        // 2. 生成唯一文件名
        String newFileName = UUID.randomUUID().toString() + suffix;

        // 3. 处理存储路径（防止配置漏掉斜杠）
        if (!uploadPath.endsWith("/") && !uploadPath.endsWith("\\")) {
            uploadPath += File.separator;
        }

        // 4. 创建文件对象
        File dest = new File(uploadPath + newFileName);

        // 确保父目录存在
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }

        try {
            // 5. 保存文件
            file.transferTo(dest);

            // 6. 【核心】拼接完整的 URL
            // 逻辑：http://localhost:8080 + /api + /files/ + fileName
            // 这样前端访问这个 URL 时，会再次请求后端的 getFile 方法
            String fileUrl = "http://localhost:" + port + contextPath + "/files/" + newFileName;

            return Result.success("上传成功", fileUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("上传失败：" + e.getMessage());
        }
    }

    /**
     * ✅ 新增：图片查看/下载接口
     * 作用：让上面的 fileUrl 能够真正访问到图片
     */
    @GetMapping("/{fileName}")
    public ResponseEntity<Resource> getFile(@PathVariable String fileName) {
        // 处理路径
        if (!uploadPath.endsWith("/") && !uploadPath.endsWith("\\")) {
            uploadPath += File.separator;
        }

        // 读取文件
        File file = new File(uploadPath + fileName);
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        // 返回文件流
        // 自动推断媒体类型 (图片显示，其他下载)
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        String name = file.getName().toLowerCase();
        if (name.endsWith(".png")) mediaType = MediaType.IMAGE_PNG;
        else if (name.endsWith(".jpg") || name.endsWith(".jpeg")) mediaType = MediaType.IMAGE_JPEG;
        else if (name.endsWith(".gif")) mediaType = MediaType.IMAGE_GIF;

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(new FileSystemResource(file));
    }
}