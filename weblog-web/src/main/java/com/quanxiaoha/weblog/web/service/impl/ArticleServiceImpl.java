package com.quanxiaoha.weblog.web.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.quanxiaoha.weblog.admin.event.ReadArticleEvent;
import com.quanxiaoha.weblog.common.domain.dos.*;
import com.quanxiaoha.weblog.common.domain.mapper.*;
import com.quanxiaoha.weblog.common.enums.ResponseCodeEnum;
import com.quanxiaoha.weblog.common.exception.BizException;
import com.quanxiaoha.weblog.common.utils.PageResponse;
import com.quanxiaoha.weblog.common.utils.Response;
import com.quanxiaoha.weblog.web.convert.ArticleConvert;
import com.quanxiaoha.weblog.web.markdown.MarkdownHelper;
import com.quanxiaoha.weblog.web.model.vo.article.*;
import com.quanxiaoha.weblog.web.model.vo.category.FindCategoryListRspVO;
import com.quanxiaoha.weblog.web.model.vo.tag.FindTagListRspVO;
import com.quanxiaoha.weblog.web.service.ArticleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private ArticleCategoryRelMapper articleCategoryRelMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private ArticleTagRelMapper articleTagRelMapper;
    @Autowired
    private TagMapper tagMapper;
    @Autowired
    private ArticleConvert articleConvert;
    @Autowired
    private ArticleContentMapper articleContentMapper;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    /**
     * 查询首页文章分页数据
     */
    @Override
    public Response findArticlePageList(FindIndexArticlePageListReqVO findIndexArticlePageListReqVO) {
        // 构造分页对象
        Long current = findIndexArticlePageListReqVO.getCurrent();
        Long size = findIndexArticlePageListReqVO.getSize();
        Page<ArticleDO> page = new Page<>(current, size);

        // 分页查询文章主体记录
        articleMapper.selectPage(page, new LambdaQueryWrapper<ArticleDO>().orderByDesc(ArticleDO::getCreateTime));

        // 获取分页数据
        List<ArticleDO> articleDOS = page.getRecords();

        List<FindIndexArticlePageListRspVO> vos = null;
        if (!CollectionUtil.isEmpty(articleDOS)) {
            // do -> vo
            vos = articleDOS.stream()
                    .map(articleDO -> articleConvert.convertDO2FindIndexArticlePageListRspVO(articleDO))
                    .collect(Collectors.toList());

            // 收集所有文章的 id 到集合里
            List<Long> articleIds = articleDOS.stream().map(ArticleDO::getId).collect(Collectors.toList());

            // 设置文章所属分类
            // 查询所有分类
            List<CategoryDO> categoryDOS = categoryMapper.selectList(null);
            // 转 Map, 方便后续根据分类 ID 拿到对应的分类名称
            Map<Long, String> categoryIdNameMap = categoryDOS.stream().collect(Collectors.toMap(CategoryDO::getId, CategoryDO::getName));

            // 根据文章 ID 批量查询所有关联记录
            List<ArticleCategoryRelDO> articleCategoryRelDOS = articleCategoryRelMapper.selectByArticleIds(articleIds);

            vos.forEach(vo -> {
                Long currArticleId = vo.getId();
                // 过滤出当前文章对应的关联数据
                Optional<ArticleCategoryRelDO> optional = articleCategoryRelDOS.stream().filter(rel -> Objects.equals(rel.getArticleId(), currArticleId)).findAny();

                // 若不为空
                if (optional.isPresent()) {
                    ArticleCategoryRelDO articleCategoryRelDO = optional.get();
                    Long categoryId = articleCategoryRelDO.getCategoryId();
                    // 通过分类 ID 从 map 中拿到对应的分类名称
                    String categoryName = categoryIdNameMap.get(categoryId);

                    FindCategoryListRspVO findCategoryListRspVO = FindCategoryListRspVO.builder()
                            .id(categoryId)
                            .name(categoryName)
                            .build();
                    // 设置到当前 vo 类中
                    vo.setCategory(findCategoryListRspVO);
                }
            });

            // 第三步：设置文章标签
            // 查询所有标签
            List<TagDO> tagDOS = tagMapper.selectList(null);
            // 转 Map, 方便后续根据标签 ID 拿到对应的标签名称
            Map<Long, String> mapIdNameMap = tagDOS.stream().collect(Collectors.toMap(TagDO::getId, TagDO::getName));

            // 拿到所有文章的标签关联记录
            List<ArticleTagRelDO> articleTagRelDOS = articleTagRelMapper.selectByArticleIds(articleIds);
            vos.forEach(vo -> {
                Long currArticleId = vo.getId();
                // 过滤出当前文章的标签关联记录
                List<ArticleTagRelDO> articleTagRelDOList = articleTagRelDOS.stream().filter(rel -> Objects.equals(rel.getArticleId(), currArticleId)).collect(Collectors.toList());

                List<FindTagListRspVO> findTagListRspVOS = Lists.newArrayList();
                // 将关联记录 DO 转 VO, 并设置对应的标签名称
                articleTagRelDOList.forEach(articleTagRelDO -> {
                    Long tagId = articleTagRelDO.getTagId();
                    String tagName = mapIdNameMap.get(tagId);

                    FindTagListRspVO findTagListRspVO = FindTagListRspVO.builder()
                            .id(tagId)
                            .name(tagName)
                            .build();
                    findTagListRspVOS.add(findTagListRspVO);
                });
                // 设置转换后的标签数据
                vo.setTags(findTagListRspVOS);
            });
        }

        return PageResponse.success(page, vos);
    }

    /**
     * 获取文章详情
     */
    @Override
    public Response findArticleDetail(FindArticleDetailReqVO findArticleDetailReqVO) {
        // 获取文章 ID
        Long articleId = findArticleDetailReqVO.getArticleId();
        // 查询文章主体记录
        ArticleDO articleDO = articleMapper.selectById(articleId);

        // 判断文章是否存在
        if (Objects.isNull(articleDO)) {
            log.warn("==> 文章不存在，articleId:{}", articleId);
            throw new BizException(ResponseCodeEnum.ARTICLE_NOT_EXISTED);
        }
        // DO -> VO
        FindArticleDetailRspVO vo = articleConvert.convertDO2ArticleDetailVO(articleDO);

        // 获取文章内容
        ArticleContentDO articleContentDO = articleContentMapper.selectByArticleId(articleId);
        String content = articleContentDO.getContent();

        // 把文章内容转为 HTML
        vo.setContent(MarkdownHelper.convertMarkdownToHtml( content));

        // 查询文章分类关联记录
        ArticleCategoryRelDO articleCategoryRelDO = articleCategoryRelMapper.selectByArticleId(articleId);
        Long categoryId = articleCategoryRelDO.getCategoryId();
        vo.setCategoryId(categoryId);

        // 查询分类名称
        CategoryDO categoryDO = categoryMapper.selectById(categoryId);
        vo.setCategoryName(categoryDO.getName());

        // 查询文章标签关联记录
        List<ArticleTagRelDO> articleTagRelDOS = articleTagRelMapper.selectByArticleId(articleId);
        List<Long> tagIds = articleTagRelDOS.stream()
                .map(ArticleTagRelDO::getTagId)
                .collect(Collectors.toList());

        // 查询标签表
        List<TagDO> tagDOS = tagMapper.selectBatchIds(tagIds);
        List<FindTagListRspVO> tags = tagDOS.stream()
                .map(tagDO -> FindTagListRspVO.builder()
                        .id(tagDO.getId()).name(tagDO.getName())
                        .build()
                ).collect(Collectors.toList());
        vo.setTags(tags);

        // 查询上一篇文章
        ArticleDO preArticleDO = articleMapper.selectPreArticle(articleId);
        if (!Objects.isNull(preArticleDO)) {
            FindPreNextArticleRspVO preArticleVO = FindPreNextArticleRspVO.builder()
                    .articleId(preArticleDO.getId())
                    .articleTitle(preArticleDO.getTitle())
                    .build();
            vo.setPreArticle(preArticleVO);
        }

        // 查询下一篇文章
        ArticleDO nextArticleDO = articleMapper.selectNextArticle(articleId);
        if (!Objects.isNull(nextArticleDO)) {
            FindPreNextArticleRspVO nextArticleVO = FindPreNextArticleRspVO.builder()
                    .articleId(nextArticleDO.getId())
                    .articleTitle(nextArticleDO.getTitle())
                    .build();
            vo.setNextArticle(nextArticleVO);
        }

        // 发布文章阅读事件
        eventPublisher.publishEvent(new ReadArticleEvent(this, articleId));

        return Response.success(vo);
    }
}
