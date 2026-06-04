package com.quanxiaoha.weblog.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.quanxiaoha.weblog.common.domain.dos.ArticleDO;
import com.quanxiaoha.weblog.common.domain.mapper.ArticleMapper;
import com.quanxiaoha.weblog.common.utils.PageResponse;
import com.quanxiaoha.weblog.common.utils.Response;
import com.quanxiaoha.weblog.web.convert.ArticleConvert;
import com.quanxiaoha.weblog.web.model.vo.archive.FindArchiveArticlePageListReqVO;
import com.quanxiaoha.weblog.web.model.vo.archive.FindArchiveArticlePageListRspVO;
import com.quanxiaoha.weblog.web.model.vo.archive.FindArchiveArticleRspVO;
import com.quanxiaoha.weblog.web.service.ArchiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ArchiveServiceImpl implements ArchiveService {

    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private ArticleConvert articleConvert;

    /**
     * 获取文章归档分页数据
     */
    @Override
    public Response findArchivePageList(FindArchiveArticlePageListReqVO findArchiveArticlePageListReqVO) {
        // 获取分页参数
        Long current = findArchiveArticlePageListReqVO.getCurrent();
        Long size = findArchiveArticlePageListReqVO.getSize();
        Page<ArticleDO> page = Page.of(current, size);
        // 执行查询
        articleMapper.selectPage(page, new LambdaQueryWrapper<ArticleDO>().orderByDesc(ArticleDO::getCreateTime));
        // 获取分页数据
        List<ArticleDO> articleDOS = page.getRecords();
        List<FindArchiveArticlePageListRspVO> vos = Lists.newArrayList();
        if (!CollectionUtils.isEmpty(articleDOS)) {
            // 将 DO 转为 VO
            List<FindArchiveArticleRspVO> archiveArticleRspVOS = articleDOS.stream()
                    .map(articleDO -> articleConvert.convertDO2ArchiveArticleVO(articleDO))
                    .collect(Collectors.toList());
            // 将年月相同的数据放到一个集合里，创建一个 Map
            Map<YearMonth, List<FindArchiveArticleRspVO>> map = archiveArticleRspVOS.stream()
                    .collect(Collectors.groupingBy(FindArchiveArticleRspVO::getCreateMonth));
            // 遍历集合，将相同年月的数据进行分组
            vos = map.entrySet().stream().map(entry -> {
                        YearMonth month = entry.getKey();
                        List<FindArchiveArticleRspVO> articles = entry.getValue();
                        return new FindArchiveArticlePageListRspVO(month, articles);
                    }).sorted(Comparator.comparing(FindArchiveArticlePageListRspVO::getMonth).reversed())
                    .collect(Collectors.toList());
        }
        return PageResponse.success(page, vos);
    }
}
