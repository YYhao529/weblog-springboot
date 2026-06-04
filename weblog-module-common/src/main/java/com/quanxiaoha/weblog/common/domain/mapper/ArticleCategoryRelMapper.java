package com.quanxiaoha.weblog.common.domain.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quanxiaoha.weblog.common.domain.dos.ArticleCategoryRelDO;

import java.util.List;

public interface ArticleCategoryRelMapper extends BaseMapper<ArticleCategoryRelDO> {

    /**
     * 根据文章 id 删除文章分类关联记录
     */
    default int deleteByArticleId(Long articleId) {
        return delete(new LambdaQueryWrapper<ArticleCategoryRelDO>()
                .eq(ArticleCategoryRelDO::getArticleId, articleId));
    }

    /**
     * 根据文章 id 查询文章分类关联记录
     */
    default ArticleCategoryRelDO selectByArticleId(Long articleId) {
        return selectOne(new LambdaQueryWrapper<ArticleCategoryRelDO>()
                .eq(ArticleCategoryRelDO::getArticleId, articleId));
    }

    /**
     * 根据分类 id 查询 1 条记录
     */
    default ArticleCategoryRelDO selectOneByCategoryId(Long categoryId) {
        return selectOne(new LambdaQueryWrapper<ArticleCategoryRelDO>()
                .eq(ArticleCategoryRelDO::getCategoryId, categoryId)
                .last("limit 1"));

    }

    /**
     * 根据文章 id 集合批量查询
     */
    default List<ArticleCategoryRelDO> selectByArticleIds(List<Long> articleIds){
        return selectList(new LambdaQueryWrapper<ArticleCategoryRelDO>()
                .in(ArticleCategoryRelDO::getArticleId, articleIds));
    }

    /**
     * 根据分类 id 查询所有关联记录
     */
    default List<ArticleCategoryRelDO> selectListByCategoryId(Long categoryId) {
        return selectList(new LambdaQueryWrapper<ArticleCategoryRelDO>()
                .eq(ArticleCategoryRelDO::getCategoryId, categoryId));
    }
}
