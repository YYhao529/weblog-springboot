package com.quanxiaoha.weblog.admin.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Maps;
import com.quanxiaoha.weblog.admin.model.vo.dashboard.FindDashboardPVStatisticsInfoRspVO;
import com.quanxiaoha.weblog.admin.model.vo.dashboard.FindDashboardStatisticsInfoRspVO;
import com.quanxiaoha.weblog.admin.service.AdminDashboardService;
import com.quanxiaoha.weblog.common.constant.DateConstants;
import com.quanxiaoha.weblog.common.domain.dos.ArticlePublishCountDO;
import com.quanxiaoha.weblog.common.domain.dos.StatisticsArticlePVDO;
import com.quanxiaoha.weblog.common.domain.mapper.ArticleMapper;
import com.quanxiaoha.weblog.common.domain.mapper.CategoryMapper;
import com.quanxiaoha.weblog.common.domain.mapper.StatisticsArticlePVMapper;
import com.quanxiaoha.weblog.common.domain.mapper.TagMapper;
import com.quanxiaoha.weblog.common.utils.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminDashboardServiceImpl implements AdminDashboardService {

    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private TagMapper tagMapper;
    @Autowired
    private StatisticsArticlePVMapper articlePVMapper;

    /**
     * 获取仪表盘基础统计信息
     */
    @Override
    public Response findDashboardStatistics() {
        // 1.统计文章总数
        Long articleTotalCount = articleMapper.selectCount(null);

        // 2.统计分类总数
        Long categoryTotalCount = categoryMapper.selectCount(null);

        // 3.统计标签总数
        Long tagTotalCount = tagMapper.selectCount(null);

        // 4.统计阅读总数
        Long pvTotalCount = articleMapper.selectAllReadNum();

        // 组装 VO 对象
        FindDashboardStatisticsInfoRspVO vo = FindDashboardStatisticsInfoRspVO.builder()
                .articleTotalCount(articleTotalCount)
                .categoryTotalCount(categoryTotalCount)
                .tagTotalCount(tagTotalCount)
                .pvTotalCount(pvTotalCount)
                .build();

        return Response.success(vo);
    }

    /**
     * 获取文章发布热点统计信息
     */
    @Override
    public Response findDashboardPublishArticleStatistics() {
        // 当前日期
        LocalDate currDate = LocalDate.now();
        // 当前日期倒退一年的日期
        LocalDate startDate = currDate.minusYears(1);

        // 查询前一年内文章发布数量
        List<ArticlePublishCountDO> articlePublishCountDOS = articleMapper.selectDateArticlePublishCount(startDate, currDate);

        Map<LocalDate, Long> map = null;
        if (!CollectionUtil.isEmpty(articlePublishCountDOS)) {
            // DO 转 Map
            Map<LocalDate, Long> dateArticleCountMap = articlePublishCountDOS.stream()
                    .collect(Collectors.toMap(ArticlePublishCountDO::getDate, ArticlePublishCountDO::getCount));

            // 有序 Map, 返回的日期文章数需要以升序排列
            map = Maps.newLinkedHashMap();
            // 从上一年的今天循环到今天
            while (!startDate.isAfter(currDate)) {
                // 以日期作为 key 从 dateArticleCountMap 中取文章发布总量
                Long count = dateArticleCountMap.get(startDate);
                map.put(startDate, Objects.isNull(count) ? 0 : count);
                startDate = startDate.plusDays(1);
            }
        }
        return Response.success(map);
    }

    /**
     * 获取文章最近一周 PV 访问量统计信息
     */
    @Override
    public Response findDashboardPVStatistics() {
        // 查询最近一周的 PV 访问量记录
        List<StatisticsArticlePVDO> articlePVDOS = articlePVMapper.selectLatestWeekRecords();

        // 把 PV 访问量记录转换成 Map，方便后续通过日期获取 PV 访问量
        Map<LocalDate, Long> pvDateCountMap = articlePVDOS.stream()
                .collect(Collectors.toMap(StatisticsArticlePVDO::getPvDate, StatisticsArticlePVDO::getPvCount));

        List<String> pvDates = new ArrayList<>(7);    // 日期集合
        List<Long> pvCounts = new ArrayList<>(7);    // PV 集合

        LocalDate currDate = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate tmpDate = currDate.minusDays(i);
            pvDates.add(tmpDate.format(DateConstants.MONTH_DAY_FORMATTER));
            pvCounts.add(pvDateCountMap.getOrDefault(tmpDate, 0L));
        }

        // 组装 VO 对象
        FindDashboardPVStatisticsInfoRspVO vo = FindDashboardPVStatisticsInfoRspVO.builder()
                .pvDates(pvDates)
                .pvCounts(pvCounts)
                .build();
        return Response.success(vo);
    }
}
