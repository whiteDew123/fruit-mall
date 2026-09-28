package com.fruitmall.common.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Getter;

import java.util.List;

/**
 * 分页响应体：{ pageNum, pageSize, total, list }
 */
@Getter
public class PageResult<T> {

    /** 当前页码，从 1 开始 */
    private final long pageNum;

    /** 每页条数 */
    private final long pageSize;

    /** 总条数 */
    private final long total;

    /** 当前页数据 */
    private final List<T> list;

    private PageResult(long pageNum, long pageSize, long total, List<T> list) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.list = list;
    }

    /** 由 MyBatis-Plus 的分页对象转换 */
    public static <T> PageResult<T> of(IPage<T> page) {
        return new PageResult<>(page.getCurrent(), page.getSize(), page.getTotal(), page.getRecords());
    }

    /** 手工组装，用于非 MyBatis-Plus 分页场景 */
    public static <T> PageResult<T> of(long pageNum, long pageSize, long total, List<T> list) {
        return new PageResult<>(pageNum, pageSize, total, list);
    }
}
