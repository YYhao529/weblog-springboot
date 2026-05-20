package com.quanxiaoha.weblog.admin.service;

import com.quanxiaoha.weblog.admin.model.vo.blogSettings.UpdateBlogSettingReqVO;
import com.quanxiaoha.weblog.common.utils.Response;

public interface AdminBlogSettingsService {
    /**
     * 更新博客设置信息
     */
    Response updateBlogSettings(UpdateBlogSettingReqVO updateBlogSettingReqVO);

    /**
     * 获取博客设置详情
     */
    Response findDetail();
}
