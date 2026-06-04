package com.quanxiaoha.weblog.admin.schedule;

import com.quanxiaoha.weblog.common.domain.dos.StatisticsArticlePVDO;
import com.quanxiaoha.weblog.common.domain.mapper.StatisticsArticlePVMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@Slf4j
public class InitPVRecordScheduledTask{

    @Autowired
    private StatisticsArticlePVMapper statisticsArticlePVMapper;

    @Scheduled(cron = "0 0 23 * * ?")   // 每天 23 点执行
    public void execute(){
        // 定时任务执行的业务逻辑
        log.info("==> 开始执行初始化明日 PV 访问量记录定时任务");

        // 当日日期
        LocalDate currDate = LocalDate.now();

        // 当日日期加 1 天
        LocalDate tomorrowDate = currDate.plusDays(1);

        // 组装插入的记录
        StatisticsArticlePVDO articlePVDO = StatisticsArticlePVDO.builder()
                .pvDate(tomorrowDate)   // 明日的文章 PV 访问量
                .pvCount(0L)    // 默认阅读量为 0
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        statisticsArticlePVMapper.insert(articlePVDO);
        log.info("==> 初始化明日 PV 访问量记录定时任务执行完毕");
    }
}
