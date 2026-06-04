package com.quanxiaoha.weblog.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class ThreadPoolConfig {

    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskExecutor(){
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);   // 线程池核心线程数
        executor.setMaxPoolSize(20);    // 线程池最大线程数
        executor.setQueueCapacity(100); // 线程池任务队列容量
        executor.setThreadNamePrefix("WeblogTaskPool-");    // 线程池线程名称前缀
        executor.initialize();
        return executor;
    }
}
