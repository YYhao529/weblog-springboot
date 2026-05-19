package com.quanxiaoha.weblog.admin.service;

import com.quanxiaoha.weblog.common.utils.Response;
import org.springframework.web.multipart.MultipartFile;

public interface AdminFileService {
    /**
     * 上传文件
     */
    Response uploadFile(MultipartFile file);
}
