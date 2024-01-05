package com.lemon.generator.service;

import com.lemon.common.core.page.PageQuery;
import com.lemon.common.core.page.TableDataInfo;
import com.lemon.generator.domain.GenTable;
import com.lemon.generator.domain.GenTableColumn;

import java.util.List;
import java.util.Map;

public interface IGenTableService {
    /**
     * 查询业务列表
     */
    TableDataInfo<GenTable> selectPageGenTableList(GenTable genTable, PageQuery pageQuery);

    /**
     * 查询代码生成业务详情
     */
    GenTable selectGenTableById(Long tableId);

    /**
     * 查询所有表信息
     */
    List<GenTable> selectGenTableAll();

    /**
     * 查询业务字段列表
     */
    List<GenTableColumn> selectGenTableColumnListByTableId(Long tableId);

    /**
     * 查询数据库列表
     */
    TableDataInfo<GenTable> selectPageDbTableList(GenTable genTable, PageQuery pageQuery);

    /**
     * 查询数据库列表
     * @param tableNames 表名称列表
     * @return 数据库表集合
     */
    List<GenTable> selectDbTableListByNames(String[] tableNames);

    /**
     * 导入表结构
     */
    void importGenTable(List<GenTable> tableList);

    /**
     * 修改时的参数校验
     */
    void validateEdit(GenTable genTable);

    /**
     * 修改业务
     */
    void updateGenTable(GenTable genTable);

    /**
     * 批量删除
     */
    void deleteGenTableByIds(Long[] tableIds);

    /**
     * 预览代码
     */
    Map<String, String> previewCode(Long tableId);

    /**
     * 生成代码（下载方式）
     */
    byte[] downloadCode(String tableName);

    /**
     * 生成代码（自定义路径）
     */
    void generatorCode(String tableName);

    /**
     * 同步数据库
     */
    void synchDb(String tableName);

    /**
     * 批量生成代码（下载方式）
     */
    byte[] downloadCode(String[] tableNames);
}
