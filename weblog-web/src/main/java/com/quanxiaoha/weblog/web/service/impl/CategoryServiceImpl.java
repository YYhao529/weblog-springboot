package com.quanxiaoha.weblog.web.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quanxiaoha.weblog.common.domain.dos.ArticleCategoryRelDO;
import com.quanxiaoha.weblog.common.domain.dos.ArticleDO;
import com.quanxiaoha.weblog.common.domain.dos.CategoryDO;
import com.quanxiaoha.weblog.common.domain.mapper.ArticleCategoryRelMapper;
import com.quanxiaoha.weblog.common.domain.mapper.ArticleMapper;
import com.quanxiaoha.weblog.common.domain.mapper.CategoryMapper;
import com.quanxiaoha.weblog.common.enums.ResponseCodeEnum;
import com.quanxiaoha.weblog.common.exception.BizException;
import com.quanxiaoha.weblog.common.utils.PageResponse;
import com.quanxiaoha.weblog.common.utils.Response;
import com.quanxiaoha.weblog.web.convert.ArticleConvert;
import com.quanxiaoha.weblog.web.model.vo.category.FindCategoryArticlePageListReqVO;
import com.quanxiaoha.weblog.web.model.vo.category.FindCategoryArticlePageListRspVO;
import com.quanxiaoha.weblog.web.model.vo.category.FindCategoryListRspVO;
import com.quanxiaoha.weblog.web.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Slf4j
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private ArticleCategoryRelMapper articleCategoryRelMapper;
    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private ArticleConvert articleConvert;

    /**
     * 获取分类列表
     */
    @Override
    public Response findCategoryList() {
        // 查询所有分类
        List<CategoryDO> categoryDOS = categoryMapper.selectList(null);

        // DO 转换为 VO
        List<FindCategoryListRspVO> vos = null;
        if (!CollectionUtil.isEmpty(categoryDOS)) {
            vos = categoryDOS.stream()
                    .map(categoryDO -> FindCategoryListRspVO.builder()
                            .id(categoryDO.getId())
                            .name(categoryDO.getName())
                            .build())
                    .collect(Collectors.toList());
        }
        return Response.success(vos);
    }

    /**
     * 获取分类下文章分页列表
     */
    @Override
    public Response findCategoryArticlePageList(FindCategoryArticlePageListReqVO findCategoryArticlePageListReqVO) {
        // 获取参数
        Long current = findCategoryArticlePageListReqVO.getCurrent();
        Long size = findCategoryArticlePageListReqVO.getSize();
        Long categoryId = findCategoryArticlePageListReqVO.getId();
        // 先判断该分类是否真实存在
        CategoryDO categoryDO = categoryMapper.selectById(categoryId);
        if (Objects.isNull(categoryDO)) {
            log.warn("==> 该分类不存在，分类 ID: {}", categoryId);
            throw new BizException(ResponseCodeEnum.CATEGORY_NOT_EXISTED);
        }
        // 根据分类 ID 查询所有文章分类关联记录
        List<ArticleCategoryRelDO> articleCategoryRelDOS = articleCategoryRelMapper.selectListByCategoryId(categoryId);
        // 若该分类下没有文章
        if (CollectionUtil.isEmpty(articleCategoryRelDOS)) {
            log.info("==> 该分类下还未发布任何文章，分类 ID: {}", categoryId);
            return PageResponse.success(null, null);
        }
        // 拿到文章 ID 集合
        List<Long> ids = articleCategoryRelDOS.stream()
                .map(ArticleCategoryRelDO::getArticleId)
                .collect(Collectors.toList());
        // 拿着文章 ID 集合批量查询文章
        Page<ArticleDO> page = articleMapper.selectPageListByArticleIds(current, size, ids);
        // 获取分页结果
        List<ArticleDO> articleDOS = page.getRecords();
        // DO 转换为 VO
        List<FindCategoryArticlePageListRspVO> vos = articleDOS.stream()
                .map(articleDO -> articleConvert.convertDO2CategoryArticleVO(articleDO))
                .collect(Collectors.toList());
        return PageResponse.success(page, vos);
    }
}
