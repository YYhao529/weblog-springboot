package com.quanxiaoha.weblog.admin.convert;

import com.quanxiaoha.weblog.admin.model.vo.article.FindArticleDetailRspVO;
import com.quanxiaoha.weblog.common.domain.dos.ArticleDO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ArticleDetailConvert {

    /**
     * 将 DO 转化为 VO
     */
    FindArticleDetailRspVO convertDO2VO(ArticleDO bean);
}
