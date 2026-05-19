package com.quanxiaoha.weblog.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quanxiaoha.weblog.admin.convert.BlogSettingsConvert;
import com.quanxiaoha.weblog.admin.model.vo.blogSettings.UpdateBlogSettingReqVO;
import com.quanxiaoha.weblog.admin.service.AdminBlogSettingsService;
import com.quanxiaoha.weblog.common.domain.dos.BlogSettingsDO;
import com.quanxiaoha.weblog.common.domain.mapper.BlogSettingsMapper;
import com.quanxiaoha.weblog.common.utils.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminBlogSettingsServiceImpl extends ServiceImpl<BlogSettingsMapper, BlogSettingsDO> implements AdminBlogSettingsService {

    @Autowired
    private BlogSettingsConvert blogSettingsConvert;

    /**
     * 更新博客设置
     */
    @Override
    public Response updateBlogSettings(UpdateBlogSettingReqVO updateBlogSettingReqVO) {
        // 构建博客设置 DO
        BlogSettingsDO blogSettingsDO = blogSettingsConvert.convertVO2DO(updateBlogSettingReqVO);
        blogSettingsDO.setId(1L);

        // 保存或更新
        saveOrUpdate(blogSettingsDO);
        return Response.success();
    }
}
