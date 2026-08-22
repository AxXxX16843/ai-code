package com.szb.aicode.common;


import lombok.Data;

import java.io.Serializable;

@Data
public class PageRequest implements Serializable {
    private int pageNum = 1;

    /**
     * 页面大小
     */
    private int pageSize = 10;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序顺序（默认降序）
     */
    private String sortOrder = "descend";
}
