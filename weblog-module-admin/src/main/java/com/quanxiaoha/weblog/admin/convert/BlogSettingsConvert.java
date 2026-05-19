package com.quanxiaoha.weblog.admin.convert;

import com.quanxiaoha.weblog.admin.model.vo.blogSettings.UpdateBlogSettingReqVO;
import com.quanxiaoha.weblog.common.domain.dos.BlogSettingsDO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BlogSettingsConvert {

    BlogSettingsDO convertVO2DO(UpdateBlogSettingReqVO updateBlogSettingReqVO);
}
