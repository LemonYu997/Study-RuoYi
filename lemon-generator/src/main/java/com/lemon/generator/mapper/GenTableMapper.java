package com.lemon.generator.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lemon.common.core.mapper.BaseMapperPlus;
import com.lemon.generator.domain.GenTable;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 数据层
 */
@InterceptorIgnore(dataPermission = "true")
public interface GenTableMapper extends BaseMapperPlus<GenTableMapper, GenTable, GenTable> {

    /**
     * 查询表ID业务信息
     */
    GenTable selectGenTableById(Long tableId);

    /**
     * 查询所有表信息
     */
    List<GenTable> selectGenTableAll();

    /**
     * 查询数据库列表
     */
    Page<GenTable> selectPageDbTableList(@Param("page") Page<Object> build, @Param("genTable") GenTable genTable);

    /**
     * 查询数据库列表
     * @param tableNames 表名称列表
     * @return 数据库表集合
     */
    List<GenTable> selectDbTableListByNames(String[] tableNames);

    /**
     * 根据表名查询
     */
    GenTable selectGenTableByName(String subTableName);
}
