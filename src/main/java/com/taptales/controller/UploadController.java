package com.taptales.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.taptales.util.Result;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class UploadController {
    @Value("${file.upload-path}")
    private String uploadPath;

    @PostMapping
    public Result upload(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        if (file == null || file.isEmpty()) {
            return Result.fail("no file");
        }
        try {
            File dir = new File(uploadPath);
            if (!dir.exists()) dir.mkdirs();
            String ext = getFileExtension(file.getOriginalFilename());
            String filename = UUID.randomUUID().toString() + ext;
            File dest = new File(dir, filename);
            file.transferTo(dest);

            // 返回完整URL，小程序可以直接使用
            String scheme = request.getScheme();
            String host = request.getServerName() + ":" + request.getServerPort();
            String url = scheme + "://" + host + "/uploads/" + filename;

            Map<String, Object> data = new HashMap<>();
            data.put("url", url);
            data.put("filename", filename);
            return Result.ok(data);
        } catch (IOException e) {
            return Result.fail("upload failed: " + e.getMessage());
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) return ".jpg";
        return filename.substring(filename.lastIndexOf("."));
    }
}
