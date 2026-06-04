package com.quanxiaoha.weblog.common.domain.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quanxiaoha.weblog.common.domain.dos.ArticleContentDO;

public interface ArticleContentMapper extends BaseMapper<ArticleContentDO> {

    /**
     * 根据文章 id 删除文章内容记录
     */
    default int deleteByArticleId(Long articleId){
        return delete(new LambdaQueryWrapper<ArticleContentDO>()
                .eq(ArticleContentDO::getArticleId, articleId));
    }

    /**
     * 根据文章 id 查询文章内容
     */
    default ArticleContentDO selectByArticleId(Long articleId){
        return selectOne(new LambdaQueryWrapper<ArticleContentDO>()
                .eq(ArticleContentDO::getArticleId, articleId));
    }

    /**
     * 根据文章 id 更新文章内容
     */
    default int updateByArticleId(ArticleContentDO articleContentDO){
        return update(articleContentDO,
                new LambdaQueryWrapper<ArticleContentDO>()
                        .eq(ArticleContentDO::getArticleId, articleContentDO.getArticleId()));
    }
}
