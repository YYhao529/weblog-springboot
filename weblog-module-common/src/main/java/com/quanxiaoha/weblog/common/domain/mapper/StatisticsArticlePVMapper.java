package com.quanxiaoha.weblog.common.domain.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.quanxiaoha.weblog.common.domain.dos.StatisticsArticlePVDO;

import java.time.LocalDate;
import java.util.List;

public interface StatisticsArticlePVMapper extends BaseMapper<StatisticsArticlePVDO> {

    /**
     * 对指定日期的文章 PV 访问量进行 +1
     */
    default int increasePVCount(LocalDate date){
        return update(null,new LambdaUpdateWrapper<StatisticsArticlePVDO>()
                .setSql("pv_count = pv_count + 1")
                .eq(StatisticsArticlePVDO::getPvDate,date));
    }


    /**
     * 查询最近一周的文章 PV 访问量记录
     */
    default List<StatisticsArticlePVDO> selectLatestWeekRecords(){
        LocalDate now = LocalDate.now();
        LocalDate sevenDaysAgo = now.minusDays(7);
        LocalDate tomorrow = now.plusDays(1);

        return selectList(new LambdaQueryWrapper<StatisticsArticlePVDO>()
                .gt(StatisticsArticlePVDO::getPvDate, sevenDaysAgo)
                .lt(StatisticsArticlePVDO::getPvDate, tomorrow)
                .orderByDesc(StatisticsArticlePVDO::getPvDate));
    }
}
