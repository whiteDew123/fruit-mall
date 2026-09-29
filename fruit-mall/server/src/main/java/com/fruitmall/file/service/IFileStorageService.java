package com.fruitmall.file.service;

import org.springframework.web.multipart.MultipartFile;

/** 文件存储服务。 */
public interface IFileStorageService {

    /**
     * 保存图片并返回可直接访问的相对地址
     *
     * @param file 上传的图片
     * @return 形如 /api/files/20260929/xxxx.jpg 的地址
     */
    String storeImage(MultipartFile file);
}
