package com.lemon.common.core.page;

import cn.hutool.http.HttpStatus;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 分页返回对象
 */
@Data
@NoArgsConstructor
public class TableDataInfo<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 总记录数
     */
    private long total;

    /**
     * 列表数据
     */
    private List<T> rows;

    /**
     * 状态码
     */
    private int code;

    /**
     * 消息
     */
    private String msg;

    /**
     * 构造函数
     */
    public TableDataInfo(List<T> list, long total) {
        this.rows = list;
        this.total = total;
    }

    /**
     * 根据 MP 分页对象构建
     */
    public static <T> TableDataInfo<T> build(IPage<T> page) {
        TableDataInfo<T> result = new TableDataInfo<>();
        result.setCode(HttpStatus.HTTP_OK);
        result.setMsg("查询成功");
        result.setRows(page.getRecords());
        result.setTotal(page.getTotal());
        return result;
    }

    /**
     * 根据 List 对象构建
     */
    public static <T> TableDataInfo<T> build(List<T> list) {
        TableDataInfo<T> result = new TableDataInfo<>();
        result.setTotal(list.size());
        result.setRows(list);
        result.setCode(HttpStatus.HTTP_OK);
        result.setMsg("查询成功");
        return result;
    }

    /**
     * 空查询
     */
    public static <T> TableDataInfo<T> build() {
        TableDataInfo<T> rspData = new TableDataInfo<>();
        rspData.setCode(HttpStatus.HTTP_OK);
        rspData.setMsg("查询成功");
        return rspData;
    }
}
