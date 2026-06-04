package com.quanxiaoha.weblog.common.domain.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quanxiaoha.weblog.common.config.InsertBatchMapper;
import com.quanxiaoha.weblog.common.domain.dos.ArticleTagRelDO;

import java.util.List;

public interface ArticleTagRelMapper extends InsertBatchMapper<ArticleTagRelDO> {

    /**
     * 根据文章 id 删除文章标签关联记录
     */
    default int deleteByArticleId(Long articleId){
        return delete(new LambdaQueryWrapper<ArticleTagRelDO>().eq(ArticleTagRelDO::getArticleId, articleId));
    }

    /**
     * 根据文章 id 查询文章标签关联记录
     */
    default List<ArticleTagRelDO> selectByArticleId(Long articleId){
        return selectList(new LambdaQueryWrapper<ArticleTagRelDO>().eq(ArticleTagRelDO::getArticleId, articleId));
    }

    /**
     * 根据标签 id 查询
     */
    default ArticleTagRelDO selectOneByTagId(Long tagId){
        return selectOne(new LambdaQueryWrapper<ArticleTagRelDO>()
                .eq(ArticleTagRelDO::getTagId, tagId)
                .last("limit 1"));
    }

    /**
     * 根据文章 id 集合批量查询
     */
    default List<ArticleTagRelDO> selectByArticleIds(List<Long> articleIds){
        return selectList(new LambdaQueryWrapper<ArticleTagRelDO>()
                .in(ArticleTagRelDO::getArticleId, articleIds));
    }

    /**
     * 根据标签 id 查询所有文章标签关联记录
     */
    default List<ArticleTagRelDO> selectByTagId(Long tagId){
        return selectList(new LambdaQueryWrapper<ArticleTagRelDO>()
                .eq(ArticleTagRelDO::getTagId, tagId));
    }
}
