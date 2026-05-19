package com.quanxiaoha.weblog.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quanxiaoha.weblog.admin.model.vo.blogSettings.UpdateBlogSettingReqVO;
import com.quanxiaoha.weblog.admin.service.AdminBlogSettingsService;
import com.quanxiaoha.weblog.common.domain.dos.BlogSettingsDO;
import com.quanxiaoha.weblog.common.domain.mapper.BlogSettingsMapper;
import com.quanxiaoha.weblog.common.utils.Response;
import org.springframework.stereotype.Service;

@Service
public class AdminBlogSettingsServiceImpl extends ServiceImpl<BlogSettingsMapper, BlogSettingsDO> implements AdminBlogSettingsService {

    /**
     * 更新博客设置
     */
    @Override
    public Response updateBlogSettings(UpdateBlogSettingReqVO updateBlogSettingReqVO) {
        // 构建博客设置 DO
        BlogSettingsDO blogSettingsDO = BlogSettingsDO.builder()
                .id(1L)
                .logo(updateBlogSettingReqVO.getLogo())
                .name(updateBlogSettingReqVO.getName())
                .author(updateBlogSettingReqVO.getAuthor())
                .introduction(updateBlogSettingReqVO.getIntroduction())
                .avatar(updateBlogSettingReqVO.getAvatar())
                .githubHomepage(updateBlogSettingReqVO.getGithubHomepage())
                .csdnHomepage(updateBlogSettingReqVO.getCsdnHomepage())
                .giteeHomepage(updateBlogSettingReqVO.getGiteeHomepage())
                .zhihuHomepage(updateBlogSettingReqVO.getZhihuHomepage())
                .build();
        // 保存或更新
        saveOrUpdate(blogSettingsDO);
        return Response.success();
    }
}
