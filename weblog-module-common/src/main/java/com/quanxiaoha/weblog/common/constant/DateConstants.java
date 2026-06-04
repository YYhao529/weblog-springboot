package com.quanxiaoha.weblog.common.constant;

import java.time.format.DateTimeFormatter;

/**
 * 全局日期相关常量
 */
public final class DateConstants {

    // 私有构造，防止外部 new 实例
    private DateConstants() {
        throw new AssertionError("禁止实例化日期常量类");
    }

    /**
     * 月-日 格式：MM-dd
     */
    public static final DateTimeFormatter MONTH_DAY_FORMATTER = DateTimeFormatter.ofPattern("MM-dd");
}