package com.coldchain.common.page;

import lombok.Data;

/**
 * 通用分页查询参数
 */
@Data
public class PageQuery {

    private long page = 1;
    private long size = 10;

    public long getPage() {
        return page < 1 ? 1 : page;
    }

    public long getSize() {
        return size < 1 ? 10 : Math.min(size, 200);
    }
}
