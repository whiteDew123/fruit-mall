package com.fruitmall.file.controller;

import com.fruitmall.common.annotation.OperLog;
import com.fruitmall.common.result.Result;
import com.fruitmall.file.service.IFileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** 文件接口：图片上传。上传结果返回可直接访问的相对地址。 */
@Tag(name = "文件管理")
@RestController
@RequestMapping("/api/admin/file")
@RequiredArgsConstructor
public class FileController {

    private final IFileStorageService fileStorageService;

    @Operation(summary = "上传图片", description = "支持 jpg / jpeg / png / gif / webp，单文件不超过 5MB")
    @OperLog(module = "文件管理", action = "上传图片")
    @PostMapping("/image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        return Result.ok("上传成功", fileStorageService.storeImage(file));
    }
}
