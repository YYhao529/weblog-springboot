package com.quanxiaoha.weblog.common.domain.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quanxiaoha.weblog.common.domain.dos.ArticleDO;
import com.quanxiaoha.weblog.common.domain.dos.ArticlePublishCountDO;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

public interface ArticleMapper extends BaseMapper<ArticleDO> {
    /**
     * 根据文章 id 批量分页查询
     */
    default Page<ArticleDO> selectPageListByArticleIds(Long current, Long size, List<Long> articleIds) {
        // 创建分页对象
        Page<ArticleDO> page = new Page<>(current, size);
        // 构造查询条件
        LambdaQueryWrapper<ArticleDO> wrapper = new LambdaQueryWrapper<ArticleDO>()
                .in(ArticleDO::getId, articleIds)
                .orderByDesc(ArticleDO::getCreateTime);
        // 执行查询
        return selectPage(page, wrapper);
    }

    /**
     * 查询上一篇文章
     */
    default ArticleDO selectPreArticle(Long articleId) {
        return selectOne(new LambdaQueryWrapper<ArticleDO>()
                .lt(ArticleDO::getId, articleId)
                .orderByDesc(ArticleDO::getId)
                .last("limit 1"));
    }

    /**
     * 查询下一篇文章
     */
    default ArticleDO selectNextArticle(Long articleId) {
        return selectOne(new LambdaQueryWrapper<ArticleDO>()
                .gt(ArticleDO::getId, articleId)
                .orderByAsc(ArticleDO::getId)
                .last("limit 1"));
    }

    /**
     * 阅读量 + 1
     */
    default int increaseReadNum(Long articleId) {
        return update(null, new LambdaUpdateWrapper<ArticleDO>()
                .setSql("read_num = read_num + 1")
                .eq(ArticleDO::getId, articleId));
    }

    /**
     * 统计所有记录的总阅读量
     */
    @Select("select sum(read_num) from t_article")
    Long selectAllReadNum();

    /**
     * 按日分组，并统计每日发布的文章数量
     */
    @Select("select DATE(create_time) AS date, count(0) AS count from t_article " +
            "where create_time > #{startDate} and create_time < #{endDate} " +
            "group by DATE(create_time)")
    List<ArticlePublishCountDO> selectDateArticlePublishCount(LocalDate startDate, LocalDate endDate);
}
