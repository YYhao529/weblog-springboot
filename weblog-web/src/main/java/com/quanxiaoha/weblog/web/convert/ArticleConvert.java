package com.quanxiaoha.weblog.web.convert;

import com.quanxiaoha.weblog.common.domain.dos.ArticleDO;
import com.quanxiaoha.weblog.web.model.vo.archive.FindArchiveArticleRspVO;
import com.quanxiaoha.weblog.web.model.vo.article.FindArticleDetailRspVO;
import com.quanxiaoha.weblog.web.model.vo.article.FindIndexArticlePageListRspVO;
import com.quanxiaoha.weblog.web.model.vo.category.FindCategoryArticlePageListRspVO;
import com.quanxiaoha.weblog.web.model.vo.tag.FindTagArticlePageListRspVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ArticleConvert {

    /**
     * 将 DO 转化为 FindIndexArticlePageListRspVO
     */
    FindIndexArticlePageListRspVO convertDO2FindIndexArticlePageListRspVO(ArticleDO bean);

    /**
     * 将 DO 转化为归档文章 VO
     */
    @Mapping(target = "createDate",expression = "java(java.time.LocalDate.from(bean.getCreateTime()))")
    @Mapping(target = "createMonth",expression = "java(java.time.YearMonth.from(bean.getCreateTime()))")
    FindArchiveArticleRspVO convertDO2ArchiveArticleVO(ArticleDO bean);

    /**
     * 将 DO 转化为分类文章 VO
     */
    @Mapping(target = "createDate",expression = "java(java.time.LocalDate.from(bean.getCreateTime()))")
    FindCategoryArticlePageListRspVO convertDO2CategoryArticleVO(ArticleDO bean);

    /**
     * 将 DO 转化为标签文章 VO
     */
    @Mapping(target = "createDate",expression = "java(java.time.LocalDate.from(bean.getCreateTime()))")
    FindTagArticlePageListRspVO convertDO2TagArticleVO(ArticleDO bean);

    /**
     * 将 DO 转化为文章详情 VO
     */
    FindArticleDetailRspVO convertDO2ArticleDetailVO(ArticleDO bean);
}
