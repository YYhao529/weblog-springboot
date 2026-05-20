package com.quanxiaoha.weblog.admin.service;

import com.quanxiaoha.weblog.admin.model.vo.article.PublishArticleReqVO;
import com.quanxiaoha.weblog.common.utils.Response;

public interface AdminArticleService {
    /**
     * 发布文章
     */
    Response publishArticle(PublishArticleReqVO publishArticleReqVO);
}
