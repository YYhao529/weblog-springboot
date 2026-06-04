package com.quanxiaoha.weblog.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.quanxiaoha.weblog.admin.convert.ArticleDetailConvert;
import com.quanxiaoha.weblog.admin.model.vo.article.*;
import com.quanxiaoha.weblog.admin.service.AdminArticleService;
import com.quanxiaoha.weblog.common.domain.dos.*;
import com.quanxiaoha.weblog.common.domain.mapper.*;
import com.quanxiaoha.weblog.common.enums.ResponseCodeEnum;
import com.quanxiaoha.weblog.common.exception.BizException;
import com.quanxiaoha.weblog.common.utils.PageResponse;
import com.quanxiaoha.weblog.common.utils.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AdminArticleServiceImpl implements AdminArticleService {

    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private ArticleContentMapper articleContentMapper;
    @Autowired
    private ArticleCategoryRelMapper articleCategoryRelMapper;
    @Autowired
    private ArticleTagRelMapper articleTagRelMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private TagMapper tagMapper;
    @Autowired
    private ArticleDetailConvert articleDetailConvert;

    /**
     * 发布文章
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response publishArticle(PublishArticleReqVO publishArticleReqVO) {
        // 构造 ArticleDO，并保存到文章表
        ArticleDO articleDO = ArticleDO.builder()
                .title(publishArticleReqVO.getTitle())
                .cover(publishArticleReqVO.getCover())
                .summary(publishArticleReqVO.getSummary())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        articleMapper.insert(articleDO);

        // 拿到插入记录的主键 ID
        Long articleId = articleDO.getId();

        // 构造 ArticleContentDO，并保存到文章内容表
        ArticleContentDO articleContentDO = ArticleContentDO.builder()
                .articleId(articleId)
                .content(publishArticleReqVO.getContent())
                .build();
        articleContentMapper.insert(articleContentDO);

        // 拿到分类 ID
        Long categoryId = publishArticleReqVO.getCategoryId();

        // 判断提交的分类是否真实存在
        CategoryDO categoryDO = categoryMapper.selectById(categoryId);
        if (Objects.isNull(categoryDO)) {
            log.warn("==> 分类不存在，categoryId：{}", categoryId);
            throw new BizException(ResponseCodeEnum.CATEGORY_NOT_EXISTED);
        }
        // 构造 ArticleCategoryRelDO，并保存到文章分类关系表
        ArticleCategoryRelDO articleCategoryRelDO = ArticleCategoryRelDO.builder()
                .articleId(articleId)
                .categoryId(categoryId)
                .build();
        articleCategoryRelMapper.insert(articleCategoryRelDO);

        // 构造 ArticleTagRelDO，并保存到文章标签关系表
        List<String> publishTags = publishArticleReqVO.getTags();
        insertTags(articleId, publishTags);

        return Response.success();
    }

    /**
     * 保存标签
     */
    private void insertTags(Long articleId, List<String> publishTags) {
        // 筛选提交的标签（表中不存在的标签）
        List<String> notExistTags = null;
        // 筛选提交的标签（表中已存在的标签）
        List<String> existedTags = null;

        // 查询出所有标签
        List<TagDO> tagDOS = tagMapper.selectList(null);

        // 如果表中还没有添加任何标签
        if (CollectionUtils.isEmpty(tagDOS)) {
            notExistTags = publishTags;
        } else {
            List<String> tagIds = tagDOS.stream().map(tagDO -> String.valueOf(tagDO.getId())).collect(Collectors.toList());
            // 表中已添加相关标签，则需要筛选
            // 通过标签 ID 来筛选，包含对应 ID 则表示提交的标签是表中存在的
            existedTags = publishTags.stream().filter(publishTag -> tagIds.contains(publishTag)).collect(Collectors.toList());
            // 否则则是不存在的
            notExistTags = publishTags.stream().filter(publishTag -> !tagIds.contains(publishTag)).collect(Collectors.toList());

            // 补充逻辑：
            // 还有一种可能：按字符串名称提交上来的标签，也有可能是表中已存在的，比如表中已经有了 Java 标签，用户提交了个 java 小写的标签，需要内部装换为 Java 标签
            Map<String, Long> tagNameIdMap = tagDOS.stream().collect(Collectors.toMap(tagDO -> tagDO.getName().toLowerCase(), TagDO::getId));

            // 使用迭代器进行安全的删除操作
            Iterator<String> iterator = notExistTags.iterator();
            while (iterator.hasNext()) {
                String notExistTag = iterator.next();
                // 转小写, 若 Map 中相同的 key，则表示该新标签是重复标签
                if (tagNameIdMap.containsKey(notExistTag.toLowerCase())) {
                    // 从不存在的标签集合中清除
                    iterator.remove();
                    // 并将对应的 ID 添加到已存在的标签集合
                    existedTags.add(String.valueOf(tagNameIdMap.get(notExistTag.toLowerCase())));
                }
            }
        }

        // 将提交的上来的，已存在于表中的标签，文章-标签关联关系入库
        if (!CollectionUtils.isEmpty(existedTags)) {
            List<ArticleTagRelDO> articleTagRelDOS = Lists.newArrayList();
            existedTags.forEach(tagId -> {
                ArticleTagRelDO articleTagRelDO = ArticleTagRelDO.builder()
                        .articleId(articleId)
                        .tagId(Long.valueOf(tagId))
                        .build();
                articleTagRelDOS.add(articleTagRelDO);
            });
            // 批量插入
            articleTagRelMapper.insertBatchSomeColumn(articleTagRelDOS);
        }

        // 将提交的上来的，不存在于表中的标签，入库保存
        if (!CollectionUtils.isEmpty(notExistTags)) {
            // 需要先将标签入库，拿到对应标签 ID 后，再把文章-标签关联关系入库
            List<ArticleTagRelDO> articleTagRelDOS = Lists.newArrayList();
            notExistTags.forEach(tagName -> {
                TagDO tagDO = TagDO.builder()
                        .name(tagName)
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .build();

                tagMapper.insert(tagDO);

                // 拿到保存的标签 ID
                Long tagId = tagDO.getId();

                // 文章-标签关联关系
                ArticleTagRelDO articleTagRelDO = ArticleTagRelDO.builder()
                        .articleId(articleId)
                        .tagId(tagId)
                        .build();
                articleTagRelDOS.add(articleTagRelDO);
            });
            // 批量插入
            articleTagRelMapper.insertBatchSomeColumn(articleTagRelDOS);
        }
    }

    /**
     * 删除文章
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response deleteArticle(DeleteArticleReqVO deleteArticleReqVO) {
        Long articleId = deleteArticleReqVO.getId();
        // 首先需要判断该文章是否真实存在
        ArticleDO articleDO = articleMapper.selectById(articleId);
        if (Objects.isNull(articleDO)) {
            log.warn("==> 文章不存在，articleId：{}", articleId);
            throw new BizException(ResponseCodeEnum.ARTICLE_NOT_EXISTED);
        }
        // 删除文章
        articleMapper.deleteById(articleId);
        // 删除文章内容
        articleContentMapper.deleteByArticleId(articleId);
        // 删除文章分类关系
        articleCategoryRelMapper.deleteByArticleId(articleId);
        // 删除文章标签关系
        articleTagRelMapper.deleteByArticleId(articleId);
        return Response.success();
    }

    /**
     * 查询文章分页列表
     */
    @Override
    public Response findArticlePageList(FindArticlePageListReqVO findArticlePageListReqVO) {
        // 获取当前页、每页大小、标题、开始时间、结束时间
        Long current = findArticlePageListReqVO.getCurrent();
        Long size = findArticlePageListReqVO.getSize();
        String title = findArticlePageListReqVO.getTitle();
        LocalDate startDate = findArticlePageListReqVO.getStartDate();
        LocalDate endDate = findArticlePageListReqVO.getEndDate();
        // 创建分页对象
        Page<ArticleDO> page = new Page<>(current, size);
        // 构造查询条件
        LambdaQueryWrapper<ArticleDO> wrapper = new LambdaQueryWrapper<ArticleDO>()
                .like(title != null, ArticleDO::getTitle, title)
                .le(endDate != null, ArticleDO::getCreateTime, endDate)
                .ge(startDate != null, ArticleDO::getCreateTime, startDate)
                .orderByDesc(ArticleDO::getCreateTime);
        // 执行查询
        articleMapper.selectPage(page, wrapper);

        // 获取查询结果
        List<ArticleDO> articleDOList = page.getRecords();

        // DO -> VO
        List<FindArticlePageListRspVO> findArticlePageListRspVOS = null;
        if (!CollectionUtils.isEmpty(articleDOList)) {
            findArticlePageListRspVOS = BeanUtil.copyToList(articleDOList, FindArticlePageListRspVO.class);
        }

        return PageResponse.success(page, findArticlePageListRspVOS);
    }

    /**
     * 查询文章详情
     */
    @Override
    public Response findArticleDetail(FindArticleDetailReqVO findArticleDetailReqVO) {
        Long articleId = findArticleDetailReqVO.getId();
        // 获取文章详情
        ArticleDO articleDO = articleMapper.selectById(articleId);
        // 判断文章是否存在
        if (Objects.isNull(articleDO)) {
            log.warn("==> 文章不存在，articleId：{}", articleId);
            throw new BizException(ResponseCodeEnum.ARTICLE_NOT_EXISTED);
        }
        // DO -> VO
        FindArticleDetailRspVO findArticleDetailRspVO = articleDetailConvert.convertDO2VO(articleDO);

        // 查询文章分类 id
        ArticleCategoryRelDO articleCategoryRelDO = articleCategoryRelMapper.selectByArticleId(articleId);
        findArticleDetailRspVO.setCategoryId(articleCategoryRelDO.getCategoryId());

        // 查询文章标签 id 集合
        List<ArticleTagRelDO> articleTagRelDOS = articleTagRelMapper.selectByArticleId(articleId);
        if (!CollectionUtils.isEmpty(articleTagRelDOS)) {
            List<Long> tagIds = articleTagRelDOS.stream().map(ArticleTagRelDO::getTagId).collect(Collectors.toList());
            findArticleDetailRspVO.setTagIds(tagIds);
        }

        // 查询文章内容
        ArticleContentDO articleContentDO = articleContentMapper.selectByArticleId(articleId);
        findArticleDetailRspVO.setContent(articleContentDO.getContent());

        // 返回结果
        return Response.success(findArticleDetailRspVO);
    }

    /**
     * 更新文章
     */
    @Override
    public Response updateArticle(UpdateArticleReqVO updateArticleReqVO) {
        // 获取参数
        Long articleId = updateArticleReqVO.getId();
        String title = updateArticleReqVO.getTitle();
        String content = updateArticleReqVO.getContent();
        String cover = updateArticleReqVO.getCover();
        String summary = updateArticleReqVO.getSummary();
        Long categoryId = updateArticleReqVO.getCategoryId();
        List<String> tags = updateArticleReqVO.getTags();

        // 创建文章 DO 类
        ArticleDO articleDO = ArticleDO.builder()
                .id(articleId)
                .title(title)
                .cover(cover)
                .summary(summary)
                .updateTime(LocalDateTime.now())
                .build();
        // 更新文章表
        int count = articleMapper.updateById(articleDO);
        // 根据更新结果判断文章是否存在
        if (count==0) {
            log.warn("==> 文章不存在，articleId：{}", articleId);
            throw new BizException(ResponseCodeEnum.ARTICLE_NOT_EXISTED);
        }

        // 创建文章内容 DO 类
        ArticleContentDO articleContentDO = ArticleContentDO.builder()
                .articleId(articleId)
                .content(content)
                .build();
        // 更新文章内容表
        articleContentMapper.updateByArticleId(articleContentDO);

        // 判断分类是否存在
        CategoryDO categoryDO = categoryMapper.selectById(categoryId);
        if (Objects.isNull(categoryDO)) {
            log.warn("==> 分类不存在，categoryId：{}", categoryId);
            throw new BizException(ResponseCodeEnum.CATEGORY_NOT_EXISTED);
        }
        // 删除文章分类关系
        articleCategoryRelMapper.deleteByArticleId(articleId);
        // 创建文章分类关系 DO 类
        ArticleCategoryRelDO articleCategoryRelDO = ArticleCategoryRelDO.builder()
                .articleId(articleId)
                .categoryId(categoryId)
                .build();
        // 插入文章分类关系
        articleCategoryRelMapper.insert(articleCategoryRelDO);

        // 删除文章标签关系
        articleTagRelMapper.deleteByArticleId(articleId);
        // 创建文章标签关系 DO 类
        insertTags(articleId, tags);

        return Response.success();
    }
}
