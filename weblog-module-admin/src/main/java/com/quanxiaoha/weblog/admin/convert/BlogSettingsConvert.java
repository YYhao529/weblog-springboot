package com.quanxiaoha.weblog.admin.convert;

import com.quanxiaoha.weblog.admin.model.vo.blogSettings.FindBlogSettingsRspVO;
import com.quanxiaoha.weblog.admin.model.vo.blogSettings.UpdateBlogSettingReqVO;
import com.quanxiaoha.weblog.common.domain.dos.BlogSettingsDO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BlogSettingsConvert {

    /**
     * 将 VO 转换为 DO
     */
    BlogSettingsDO convertVO2DO(UpdateBlogSettingReqVO updateBlogSettingReqVO);

    /**
     * 将 DO 转化为 VO
     */
    FindBlogSettingsRspVO convertDO2VO(BlogSettingsDO blogSettingsDO);
}
