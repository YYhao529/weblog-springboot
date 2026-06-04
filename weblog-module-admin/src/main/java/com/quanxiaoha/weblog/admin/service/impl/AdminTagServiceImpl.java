package com.quanxiaoha.weblog.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quanxiaoha.weblog.admin.model.vo.tag.*;
import com.quanxiaoha.weblog.admin.service.AdminTagService;
import com.quanxiaoha.weblog.common.domain.dos.ArticleTagRelDO;
import com.quanxiaoha.weblog.common.domain.dos.TagDO;
import com.quanxiaoha.weblog.common.domain.mapper.ArticleTagRelMapper;
import com.quanxiaoha.weblog.common.domain.mapper.TagMapper;
import com.quanxiaoha.weblog.common.enums.ResponseCodeEnum;
import com.quanxiaoha.weblog.common.exception.BizException;
import com.quanxiaoha.weblog.common.model.vo.SelectRspVO;
import com.quanxiaoha.weblog.common.utils.PageResponse;
import com.quanxiaoha.weblog.common.utils.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.quanxiaoha.weblog.common.enums.ResponseCodeEnum.TAG_CANT_DUPLICATE;
import static com.quanxiaoha.weblog.common.enums.ResponseCodeEnum.TAG_NOT_EXISTED;

@Service
@Slf4j
public class AdminTagServiceImpl extends ServiceImpl<TagMapper, TagDO> implements AdminTagService {

    @Autowired
    private TagMapper tagMapper;
    @Autowired
    private ArticleTagRelMapper articleTagRelMapper;

    /**
     * 添加标签集合
     */
    @Transactional
    @Override
    public Response addTags(AddTagReqVO addTagReqVO) {
        // vo -> do
        List<TagDO> tagDOList = addTagReqVO.getTags()
                .stream().map(tagName -> TagDO.builder()
                        .name(tagName.trim())   // 去掉前后空格
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .build())
                .collect(Collectors.toList());

        // 批量插入
        try {
            saveBatch(tagDOList);
        } catch (Exception e) {
            log.error("标签已存在", e);
            return Response.fail(TAG_CANT_DUPLICATE);
        }
        return Response.success();
    }

    /**
     * 标签分页数据获取
     */
    @Override
    public PageResponse findTagPageList(FindTagPageListReqVO findTagPageListReqVO) {
        // 初始化分页对象
        Page<TagDO> page = Page.of(findTagPageListReqVO.getCurrent(), findTagPageListReqVO.getSize());

        // 构造查询条件
        LambdaQueryWrapper<TagDO> wrapper = new LambdaQueryWrapper<TagDO>();
        String name = findTagPageListReqVO.getName();
        LocalDate startDate = findTagPageListReqVO.getStartDate();
        LocalDate endDate = findTagPageListReqVO.getEndDate();
        if (startDate != null) {
            LocalDateTime startTime = LocalDateTime.of(startDate, LocalTime.MIN);
            wrapper.ge(TagDO::getCreateTime, startTime);
        }
        if (endDate != null) {
            LocalDateTime endTime = LocalDateTime.of(endDate, LocalTime.MAX);
            wrapper.le(TagDO::getCreateTime, endTime);
        }

        wrapper.like(Objects.nonNull(name), TagDO::getName, name)
                .orderByDesc(TagDO::getCreateTime)
                .select(TagDO::getId, TagDO::getName, TagDO::getCreateTime);

        // 执行查询
        tagMapper.selectPage(page, wrapper);
        // 获取查询结果
        List<TagDO> tagPageListRspVOList = page.getRecords();

        // DO -> VO
        List<FindTagPageListRspVO> findTagPageListRspVOS = null;
        if (!CollectionUtils.isEmpty(tagPageListRspVOList)) {
            findTagPageListRspVOS = tagPageListRspVOList.stream().map(tagDO -> FindTagPageListRspVO.builder()
                            .id(tagDO.getId())
                            .name(tagDO.getName())
                            .createTime(tagDO.getCreateTime())
                            .build())
                    .collect(Collectors.toList());
        }

        return PageResponse.success(page, findTagPageListRspVOS);
    }

    /**
     * 删除标签
     */
    @Override
    public Response deleteTag(DeleteTagReqVO deleteTagReqVO) {
        // 获取标签 id
        Long id = deleteTagReqVO.getId();
        // 判断标签是否被文章引用
        ArticleTagRelDO articleTagRelDO = articleTagRelMapper.selectOneByTagId(id);
        if (Objects.nonNull(articleTagRelDO)) {
            log.warn("==> 标签被文章引用，无法删除，tagId：{}", id);
            throw new BizException(ResponseCodeEnum.TAG_CAN_NOT_DELETE);
        }
        // 删除标签
        int count = tagMapper.deleteById(id);

        return count == 1 ? Response.success() : Response.fail(TAG_NOT_EXISTED);
    }

    /**
     * 标签模糊查询
     */
    @Override
    public Response searchTag(SearchTagReqVO searchTagReqVO) {
        String key = searchTagReqVO.getKey();
        // 构造查询条件
        LambdaQueryWrapper<TagDO> wrapper = new LambdaQueryWrapper<TagDO>();
        wrapper.like(TagDO::getName, key)
                .orderByDesc(TagDO::getCreateTime);

        // 执行查询
        List<TagDO> tagDOS = tagMapper.selectList(wrapper);

        // do -> vo
        List<SelectRspVO> selectRspVOS = null;
        if (!CollectionUtils.isEmpty(tagDOS)) {
            selectRspVOS = tagDOS.stream().map(tagDO -> SelectRspVO.builder()
                            .label(tagDO.getName())
                            .value(tagDO.getId())
                            .build())
                    .collect(Collectors.toList());
        }

        return Response.success(selectRspVOS);
    }

    /**
     * 分类 Select 下拉列表数据获取
     */
    @Override
    public Response findTagSelectList() {
        // 查询所有标签
        List<TagDO> tagDOS = tagMapper.selectList(null);

        // DO -> VO
        List<SelectRspVO> vos = null;
        if (!CollectionUtils.isEmpty(tagDOS)) {
            vos = tagDOS.stream().map(tagDO -> SelectRspVO.builder()
                            .label(tagDO.getName())
                            .value(tagDO.getId())
                            .build())
                    .collect(Collectors.toList());
        }

        return Response.success(vos);
    }
}
