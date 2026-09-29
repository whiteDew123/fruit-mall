package com.fruitmall.file.service.impl;

import com.fruitmall.common.exception.BizException;
import com.fruitmall.common.result.ResultCode;
import com.fruitmall.file.service.IFileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * 本地磁盘文件存储。
 * 演示与答辩场景使用单机磁盘即可，后续换对象存储只需替换本实现。
 */
@Slf4j
@Service
public class LocalFileStorageServiceImpl implements IFileStorageService {

    /** 允许的图片扩展名 */
    private static final List<String> ALLOWED_EXTENSIONS =
            List.of("jpg", "jpeg", "png", "gif", "webp");

    private static final DateTimeFormatter DATE_DIR = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final Path uploadRoot;
    private final String accessPrefix;

    public LocalFileStorageServiceImpl(@Value("${fruit-mall.file.upload-dir:./upload}") String uploadDir,
                                       @Value("${fruit-mall.file.access-prefix:/api/files}") String accessPrefix) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.accessPrefix = accessPrefix;
        try {
            Files.createDirectories(this.uploadRoot);
            log.info("图片上传目录：{}", this.uploadRoot);
        } catch (IOException e) {
            throw new IllegalStateException("创建上传目录失败：" + this.uploadRoot, e);
        }
    }

    @Override
    public String storeImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException(ResultCode.BAD_REQUEST, "请选择要上传的图片");
        }
        String extension = resolveExtension(file.getOriginalFilename());
        String dateDir = LocalDate.now().format(DATE_DIR);
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        Path targetDir = uploadRoot.resolve(dateDir);
        try {
            Files.createDirectories(targetDir);
            file.transferTo(targetDir.resolve(fileName).toFile());
        } catch (IOException e) {
            log.error("图片保存失败：{}", file.getOriginalFilename(), e);
            throw new BizException(ResultCode.ERROR, "图片保存失败，请重试");
        }
        return accessPrefix + "/" + dateDir + "/" + fileName;
    }

    private String resolveExtension(String originalFilename) {
        String extension = StringUtils.getFilenameExtension(originalFilename);
        if (!StringUtils.hasText(extension)) {
            throw new BizException(ResultCode.BAD_REQUEST, "无法识别图片格式");
        }
        extension = extension.toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BizException(ResultCode.BAD_REQUEST,
                    "只支持 " + String.join(" / ", ALLOWED_EXTENSIONS) + " 格式的图片");
        }
        return extension;
    }
}
