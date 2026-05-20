package com.quanxiaoha.weblog.admin.controller;

import com.quanxiaoha.weblog.admin.model.vo.blogSettings.UpdateBlogSettingReqVO;
import com.quanxiaoha.weblog.admin.service.AdminBlogSettingsService;
import com.quanxiaoha.weblog.common.aspect.ApiOperationLog;
import com.quanxiaoha.weblog.common.utils.Response;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/blog/settings")
@Api(tags = "Admin 博客设置模块")
public class AdminBlogSettingsController {

    @Autowired
    private AdminBlogSettingsService blogSettingsService;

    @PostMapping("/update")
    @ApiOperation("博客基础信息修改")
    @ApiOperationLog(description = "博客基础信息修改")
    public Response updateBlogSettings(@RequestBody @Validated UpdateBlogSettingReqVO updateBlogSettingReqVO) {
        return blogSettingsService.updateBlogSettings(updateBlogSettingReqVO);
    }

    @GetMapping("/detail")
    @ApiOperation("获取博客设置详情")
    @ApiOperationLog(description = "获取博客设置详情")
    public Response findDetail() {
        return blogSettingsService.findDetail();
    }
}
