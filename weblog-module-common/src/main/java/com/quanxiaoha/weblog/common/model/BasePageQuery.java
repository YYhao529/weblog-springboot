package com.quanxiaoha.weblog.common.model;

import lombok.Data;

@Data
public class BasePageQuery {

    /**
     * 当前页码，默认第一页
     */
    private Long current = 1L;

    /**
     * 每页显示的条数，默认 10
     */
    private Long size = 10L;
}
