package com.quanxiaoha.weblog.web.service;

import com.quanxiaoha.weblog.common.utils.Response;
import com.quanxiaoha.weblog.web.model.vo.tag.FindTagArticlePageListReqVO;

public interface TagService {

    /**
     * 获取标签列表
     */
    Response findTagList();

    /**
     * 获取标签下文章分页数据
     */
    Response findTagArticlePageList(FindTagArticlePageListReqVO findTagArticlePageListReqVO);
}
