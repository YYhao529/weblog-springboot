package com.quanxiaoha.weblog.web.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quanxiaoha.weblog.common.domain.dos.ArticleDO;
import com.quanxiaoha.weblog.common.domain.dos.ArticleTagRelDO;
import com.quanxiaoha.weblog.common.domain.dos.TagDO;
import com.quanxiaoha.weblog.common.domain.mapper.ArticleMapper;
import com.quanxiaoha.weblog.common.domain.mapper.ArticleTagRelMapper;
import com.quanxiaoha.weblog.common.domain.mapper.TagMapper;
import com.quanxiaoha.weblog.common.enums.ResponseCodeEnum;
import com.quanxiaoha.weblog.common.exception.BizException;
import com.quanxiaoha.weblog.common.utils.PageResponse;
import com.quanxiaoha.weblog.common.utils.Response;
import com.quanxiaoha.weblog.web.convert.ArticleConvert;
import com.quanxiaoha.weblog.web.model.vo.tag.FindTagArticlePageListReqVO;
import com.quanxiaoha.weblog.web.model.vo.tag.FindTagArticlePageListRspVO;
import com.quanxiaoha.weblog.web.model.vo.tag.FindTagListRspVO;
import com.quanxiaoha.weblog.web.service.TagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TagServiceImpl implements TagService {

    @Autowired
    private TagMapper tagMapper;
    @Autowired
    private ArticleTagRelMapper articleTagRelMapper;
    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private ArticleConvert articleConvert;

    /**
     * 获取标签列表
     */
    @Override
    public Response findTagList() {
        // 查询所有标签
        List<TagDO> tagDOS = tagMapper.selectList(null);

        // DO 转换为 VO
        List<FindTagListRspVO> vos = null;
        if (!CollectionUtils.isEmpty(tagDOS)) {
            vos = tagDOS.stream()
                    .map(tagDO -> FindTagListRspVO.builder()
                            .id(tagDO.getId())
                            .name(tagDO.getName())
                            .build()).collect(Collectors.toList());
        }
        return Response.success(vos);
    }

    /**
     * 获取标签下文章分页列表
     */
    @Override
    public Response findTagArticlePageList(FindTagArticlePageListReqVO findTagArticlePageListReqVO) {
        // 获取参数
        Long current = findTagArticlePageListReqVO.getCurrent();
        Long size = findTagArticlePageListReqVO.getSize();
        Long tagId = findTagArticlePageListReqVO.getId();
        // 先判断该标签是否真实存在
        TagDO tagDO = tagMapper.selectById(tagId);
        if (Objects.isNull(tagDO)) {
            log.warn("==> 该标签不存在，标签 ID: {}", tagId);
            throw new BizException(ResponseCodeEnum.TAG_NOT_EXISTED);
        }
        // 根据标签 id 查询所有文章标签关联记录
        List<ArticleTagRelDO> articleTagRelDOS = articleTagRelMapper.selectByTagId(tagId);
        if (CollectionUtils.isEmpty(articleTagRelDOS)) {
            log.info("==> 该标签下还未发布任何文章，标签 ID: {}", tagId);
            return PageResponse.success(null, null);
        }
        // 获取出文章 id 集合
        List<Long> articleIds = articleTagRelDOS.stream().map(ArticleTagRelDO::getArticleId).collect(Collectors.toList());
        // 根据 id 批量查询文章
        Page<ArticleDO> page = articleMapper.selectPageListByArticleIds(current, size, articleIds);
        // 获取分页结果
        List<ArticleDO> articleDOS = page.getRecords();
        // DO 转换为 VO
        List<FindTagArticlePageListRspVO> vos = articleDOS.stream()
                .map(articleDO -> articleConvert.convertDO2TagArticleVO(articleDO))
                .collect(Collectors.toList());
        return PageResponse.success(page, vos);
    }
}
