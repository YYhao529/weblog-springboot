package com.quanxiaoha.weblog.web.service.impl;

import com.quanxiaoha.weblog.admin.convert.BlogSettingsConvert;
import com.quanxiaoha.weblog.admin.model.vo.blogSettings.FindBlogSettingsRspVO;
import com.quanxiaoha.weblog.common.domain.dos.BlogSettingsDO;
import com.quanxiaoha.weblog.common.domain.mapper.BlogSettingsMapper;
import com.quanxiaoha.weblog.common.utils.Response;
import com.quanxiaoha.weblog.web.service.BlogSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BlogSettingsServiceImpl implements BlogSettingsService {

    @Autowired
    private BlogSettingsMapper blogSettingsMapper;
    @Autowired
    private BlogSettingsConvert blogSettingsConvert;

    /**
     * 获取博客详情
     */
    @Override
    public Response findDetail() {
        // 查询 ID 为 1 的记录
        BlogSettingsDO blogSettingsDO = blogSettingsMapper.selectById(1L);
        // DO 转 VO
        FindBlogSettingsRspVO vo = blogSettingsConvert.convertDO2VO(blogSettingsDO);

        return Response.success(vo);
    }
}
