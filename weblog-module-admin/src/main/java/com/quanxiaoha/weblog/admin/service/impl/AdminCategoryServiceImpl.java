package com.quanxiaoha.weblog.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quanxiaoha.weblog.admin.model.vo.category.AddCategoryReqVO;
import com.quanxiaoha.weblog.admin.model.vo.category.DeleteCategoryReqVO;
import com.quanxiaoha.weblog.admin.model.vo.category.FindCategoryPageListReqVO;
import com.quanxiaoha.weblog.admin.model.vo.category.FindCategoryPageListRspVO;
import com.quanxiaoha.weblog.admin.service.AdminCategoryService;
import com.quanxiaoha.weblog.common.domain.dos.CategoryDO;
import com.quanxiaoha.weblog.common.domain.mapper.CategoryMapper;
import com.quanxiaoha.weblog.common.enums.ResponseCodeEnum;
import com.quanxiaoha.weblog.common.exception.BizException;
import com.quanxiaoha.weblog.common.model.vo.SelectRspVO;
import com.quanxiaoha.weblog.common.utils.PageResponse;
import com.quanxiaoha.weblog.common.utils.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AdminCategoryServiceImpl implements AdminCategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 添加分类
     */
    @Override
    public Response addCategory(AddCategoryReqVO addCategoryReqVO) {
        String categoryName = addCategoryReqVO.getName();

        CategoryDO categoryDO = categoryMapper.selectByName(categoryName);
        // 先判断该分类是否已经存在
        if (Objects.nonNull(categoryDO)) {
            log.warn("分类名称： {},此分类已存在", categoryName);
            throw new BizException(ResponseCodeEnum.CATEGORY_NAME_IS_EXISTED);
        }

        // 构建 DO 类
        CategoryDO insertCategoryDO = CategoryDO.builder()
                .name(addCategoryReqVO.getName().trim()).build();

        // 执行 insert
        categoryMapper.insert(insertCategoryDO);

        return Response.success();
    }

    /**
     * 分类分页数据查询
     */
    @Override
    public PageResponse findCategoryPageList(FindCategoryPageListReqVO findCategoryPageListReqVO) {
        Long current = findCategoryPageListReqVO.getCurrent();  // 当前页
        Long size = findCategoryPageListReqVO.getSize();    // 每页显示的条数

        Page<CategoryDO> page = Page.of(current, size); // 创建分页对象

        // 构建查询条件
        LambdaQueryWrapper<CategoryDO> wrapper = new LambdaQueryWrapper<>();
        String name = findCategoryPageListReqVO.getName();
        LocalDate startDate = findCategoryPageListReqVO.getStartDate();
        LocalDate endDate = findCategoryPageListReqVO.getEndDate();
        wrapper.like(name != null, CategoryDO::getName, name)   // like 模块查询
                .ge(startDate != null, CategoryDO::getCreateTime, startDate)    // 大于等于 startDate
                .le(endDate != null, CategoryDO::getCreateTime, endDate)    // 小于等于 endDate
                .select(CategoryDO::getId, CategoryDO::getName, CategoryDO::getCreateTime)
                .orderByDesc(CategoryDO::getCreateTime);    // 按照创建时间倒序

        // 执行分页查询
        categoryMapper.selectPage(page, wrapper);
        // 获取查询结果
        List<CategoryDO> categoryDOS = page.getRecords();

        // DO -> VO
        List<FindCategoryPageListRspVO> vos = null;
        if (!CollectionUtils.isEmpty(categoryDOS)) {
            vos = categoryDOS.stream()
                    .map(categoryDO -> FindCategoryPageListRspVO.builder()
                            .id(categoryDO.getId())
                            .name(categoryDO.getName())
                            .createTime(categoryDO.getCreateTime())
                            .build())
                    .collect(Collectors.toList());
        }

        // 返回分页结果
        return PageResponse.success(page, vos);
    }

    /**
     * 删除分类
     */
    @Override
    public Response deleteCategory(DeleteCategoryReqVO deleteCategoryReqVO) {
        // 分类 id
        Long categoryId = deleteCategoryReqVO.getId();

        // 删除分类
        categoryMapper.deleteById(categoryId);

        return Response.success();
    }

    /**
     * 获取文章分类的 Select 列表数据
     */
    @Override
    public Response findCategorySelectList() {
        // 查询所有分类
        List<CategoryDO> categoryDOS = categoryMapper.selectList(null);

        // DO -> VO
        List<SelectRspVO> selectRspVOS = null;
        if (!CollectionUtils.isEmpty(categoryDOS)){
            // 将分类 id 作为 value 值，将分类名称作为 label 展示
            selectRspVOS = categoryDOS.stream()
                    .map(categoryDO -> SelectRspVO.builder()
                            .label(categoryDO.getName())
                            .value(categoryDO.getId())
                            .build())
                    .collect(Collectors.toList());
        }

        return Response.success(selectRspVOS);
    }
}
