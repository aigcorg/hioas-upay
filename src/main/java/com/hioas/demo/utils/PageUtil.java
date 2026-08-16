package com.hioas.demo.utils;

import lombok.Data;

/**
 * 分页工具类
 */
@Data
public class PageUtil {

    private Integer page;
    private Integer size;
    private Long total;
    private Integer totalPage;

    public PageUtil() {
    }

    public PageUtil(Integer page, Integer size, Long total) {
        this.page = page != null ? page : 1;
        this.size = size != null ? size : 20;
        this.total = total != null ? total : 0L;
        this.totalPage = total != null && size != null && size > 0 ? (int)Math.ceil(total / (double)size) : 0;
    }

    public int getOffset() {
        return (page - 1) * size;
    }

    public static PageUtil of(Integer page, Integer size, Long total) {
        return new PageUtil(page, size, total);
    }
}
