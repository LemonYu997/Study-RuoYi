package com.lemon.generator.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lemon.common.constant.Constants;
import com.lemon.common.constant.GenConstants;
import com.lemon.common.core.page.PageQuery;
import com.lemon.common.core.page.TableDataInfo;
import com.lemon.common.exception.ServiceException;
import com.lemon.common.helper.LoginHelper;
import com.lemon.common.utils.JsonUtils;
import com.lemon.common.utils.StreamUtils;
import com.lemon.common.utils.StringUtils;
import com.lemon.common.utils.file.FileUtils;
import com.lemon.generator.domain.GenTable;
import com.lemon.generator.domain.GenTableColumn;
import com.lemon.generator.mapper.GenTableColumnMapper;
import com.lemon.generator.mapper.GenTableMapper;
import com.lemon.generator.util.GenUtils;
import com.lemon.generator.util.VelocityInitializer;
import com.lemon.generator.util.VelocityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 代码生成
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class GenTableServiceImpl implements IGenTableService {

    private final GenTableMapper genTableMapper;
    private final GenTableColumnMapper genTableColumnMapper;
    private final IdentifierGenerator identifierGenerator;

    /**
     * 查询业务列表
     */
    @Override
    public TableDataInfo<GenTable> selectPageGenTableList(GenTable genTable, PageQuery pageQuery) {
        Page<GenTable> page = genTableMapper.selectPage(pageQuery.build(), this.buildGenTableQueryWrapper(genTable));
        return TableDataInfo.build(page);
    }

    /**
     * 查询代码生成业务详情
     */
    @Override
    public GenTable selectGenTableById(Long tableId) {
        GenTable genTable = genTableMapper.selectGenTableById(tableId);
        setTableFromOptions(genTable);
        return genTable;
    }

    /**
     * 查询所有表信息
     */
    @Override
    public List<GenTable> selectGenTableAll() {
        return genTableMapper.selectGenTableAll();
    }

    /**
     * 查询业务字段列表
     */
    @Override
    public List<GenTableColumn> selectGenTableColumnListByTableId(Long tableId) {
        return genTableColumnMapper.selectList(new LambdaQueryWrapper<GenTableColumn>()
            .eq(GenTableColumn::getTableId, tableId)
            .orderByAsc(GenTableColumn::getSort));
    }

    /**
     * 查询数据库列表
     */
    @Override
    public TableDataInfo<GenTable> selectPageDbTableList(GenTable genTable, PageQuery pageQuery) {
        Page<GenTable> page = genTableMapper.selectPageDbTableList(pageQuery.build(), genTable);
        return TableDataInfo.build(page);
    }

    /**
     * 查询数据库列表
     *
     * @param tableNames 表名称列表
     * @return 数据库表集合
     */
    @Override
    public List<GenTable> selectDbTableListByNames(String[] tableNames) {
        return genTableMapper.selectDbTableListByNames(tableNames);
    }

    /**
     * 导入表结构
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importGenTable(List<GenTable> tableList) {
        String username = LoginHelper.getUsername();
        try {
            for (GenTable genTable : tableList) {
                String tableName = genTable.getTableName();
                // 初始化表信息
                GenUtils.initTable(genTable, username);
                int row = genTableMapper.insert(genTable);
                if (row > 0) {
                    // 保存列信息
                    List<GenTableColumn> genTableColumns = genTableColumnMapper.selectDbTableColumnsByName(tableName);
                    List<GenTableColumn> saveColumns = new ArrayList<>();
                    for (GenTableColumn column : genTableColumns) {
                        // 初始化表字段
                        GenUtils.initColumnField(column, genTable);
                        saveColumns.add(column);
                    }
                    if (CollUtil.isNotEmpty(saveColumns)) {
                        genTableColumnMapper.insertBatch(saveColumns);
                    }
                }
            }
        } catch (Exception e) {
            throw new ServiceException("导入失败：" + e.getMessage());
        }
    }

    /**
     * 修改时的参数校验
     */
    @Override
    public void validateEdit(GenTable genTable) {
        if (GenConstants.TPL_TREE.equals(genTable.getTplCategory())) {
            String options = JsonUtils.toJsonString(genTable.getParams());
            Dict paramsObj = JsonUtils.parseMap(options);
            if (StringUtils.isEmpty(paramsObj.getStr(GenConstants.TREE_CODE))) {
                throw new ServiceException("树编码字段不能为空");
            } else if (StringUtils.isEmpty(paramsObj.getStr(GenConstants.TREE_PARENT_CODE))) {
                throw new ServiceException("树父编码字段不能为空");
            } else if (StringUtils.isEmpty(paramsObj.getStr(GenConstants.TREE_NAME))) {
                throw new ServiceException("树名称字段不能为空");
            } else if (GenConstants.TPL_SUB.equals(genTable.getTplCategory())) {
                if (StringUtils.isEmpty(genTable.getSubTableName())) {
                    throw new ServiceException("关联子表的表名不能为空");
                } else if (StringUtils.isEmpty(genTable.getSubTableFkName())) {
                    throw new ServiceException("子表关联的外键名不能为空");
                }
            }
        }
    }

    /**
     * 修改业务
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGenTable(GenTable genTable) {
        String options = JsonUtils.toJsonString(genTable.getParams());
        genTable.setOptions(options);
        int row = genTableMapper.updateById(genTable);
        if (row > 0) {
            for (GenTableColumn column : genTable.getColumns()) {
                genTableColumnMapper.updateById(column);
            }
        }
    }

    /**
     * 批量删除
     */
    @Override
    public void deleteGenTableByIds(Long[] tableIds) {
        List<Long> ids = Arrays.asList(tableIds);
        genTableMapper.deleteBatchIds(ids);
        genTableColumnMapper.delete(Wrappers.<GenTableColumn>lambdaQuery()
            .in(GenTableColumn::getTableId, ids));
    }

    /**
     * 预览代码
     */
    @Override
    public Map<String, String> previewCode(Long tableId) {
        Map<String, String> dataMap = new LinkedHashMap<>();
        // 查询表信息
        GenTable table = genTableMapper.selectGenTableById(tableId);
        List<Long> menuIds = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            // 填充id
            menuIds.add(identifierGenerator.nextId(null).longValue());
        }
        table.setMenuIds(menuIds);
        // 设置子表信息
        setSubTable(table);
        // 设置主键列信息
        setPkColumn(table);
        //初始化 VM 方法
        VelocityInitializer.initVelocity();

        VelocityContext context = VelocityUtils.prepareContext(table);

        // 获取模版列表
        List<String> templates = VelocityUtils.getTemplateList(table.getTplCategory());
        for (String template : templates) {
            // 渲染模版
            StringWriter sw = new StringWriter();
            Template tpl = Velocity.getTemplate(template, Constants.UTF8);
            tpl.merge(context, sw);
            dataMap.put(template, sw.toString());
        }

        return dataMap;
    }

    /**
     * 生成代码（下载方式）
     */
    @Override
    public byte[] downloadCode(String tableName) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(outputStream);
        generatorCode(tableName, zip);
        IoUtil.close(zip);
        return outputStream.toByteArray();
    }

    /**
     * 生成代码（自定义路径）
     */
    @Override
    public void generatorCode(String tableName) {
        // 查询表信息
        GenTable table = genTableMapper.selectGenTableByName(tableName);
        // 设置子表信息
        setSubTable(table);
        // 设置主键列信息
        setPkColumn(table);
        //初始化 VM 方法
        VelocityInitializer.initVelocity();

        VelocityContext context = VelocityUtils.prepareContext(table);

        // 获取模版列表
        List<String> templates = VelocityUtils.getTemplateList(table.getTplCategory());
        for (String template : templates) {
            // 渲染模版
            StringWriter sw = new StringWriter();
            Template tpl = Velocity.getTemplate(template, Constants.UTF8);
            tpl.merge(context, sw);
            // 添加到 zip
            try {
                String path = getGenPath(table, template);
                FileUtils.writeUtf8String(sw.toString(), path);
            } catch (Exception e) {
                log.error("渲染模版失败，表名：" + table.getTableName(), e);
            }
        }
    }

    /**
     * 同步数据库
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void synchDb(String tableName) {
        GenTable table = genTableMapper.selectGenTableByName(tableName);
        List<GenTableColumn> columns = table.getColumns();
        Map<String, GenTableColumn> tableColumnMap = StreamUtils.toIdentityMap(columns, GenTableColumn::getColumnName);

        List<GenTableColumn> dbTableColumns = genTableColumnMapper.selectDbTableColumnsByName(tableName);
        if (CollUtil.isEmpty(dbTableColumns)) {
            throw new ServiceException("同步数据失败，原表结构不存在");
        }
        List<String> dbTableColumnNames = StreamUtils.toList(dbTableColumns, GenTableColumn::getColumnName);

        List<GenTableColumn> saveColumns = new ArrayList<>();
        columns.forEach(column -> {
            GenUtils.initColumnField(column, table);
            if (tableColumnMap.containsKey(column.getColumnName())) {
                GenTableColumn prevColumn = tableColumnMap.get(column.getColumnName());
                column.setColumnId(prevColumn.getColumnId());
                if (column.isList()) {
                    // 如果是列表，继续保留查询方式 / 字典类型选项
                    column.setDictType(prevColumn.getDictType());
                    column.setQueryType(prevColumn.getQueryType());
                }
                if (StringUtils.isNotEmpty(prevColumn.getIsRequired()) && !column.isPk()
                    && (column.isInsert() || column.isEdit())
                    && ((column.isUsableColumn()) || (!column.isSuperColumn()))) {
                    // 如果是（新增、修改、非主键、非忽略及父属性，继续保留必填、显示类型选项
                    column.setIsRequired(prevColumn.getIsRequired());
                    column.setHtmlType(prevColumn.getHtmlType());
                }
            }
            saveColumns.add(column);
        });
        if (CollUtil.isNotEmpty(saveColumns)) {
            genTableColumnMapper.insertOrUpdateBatch(saveColumns);
        }
        List<GenTableColumn> delColumns = StreamUtils.filter(columns, column -> !dbTableColumnNames.contains(column.getColumnName()));
        if (CollUtil.isNotEmpty(delColumns)) {
            List<Long> ids = StreamUtils.toList(delColumns, GenTableColumn::getColumnId);
            genTableColumnMapper.deleteBatchIds(ids);
        }
    }

    /**
     * 批量生成代码（下载方式）
     */
    @Override
    public byte[] downloadCode(String[] tableNames) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(outputStream);
        for (String tableName : tableNames) {
            generatorCode(tableName, zip);
        }
        IoUtil.close(zip);
        return outputStream.toByteArray();
    }

    /**
     * 获取代码生成地址
     */
    private String getGenPath(GenTable table, String template) {
        String genPath = table.getGenPath();
        if (StringUtils.equals(genPath, "/")) {
            return System.getProperty("user.dir") + File.separator + "src" + File.separator + VelocityUtils.getFileName(template, table);
        }
        return genPath + File.separator + VelocityUtils.getFileName(template, table);
    }

    /**
     * 查询表信息并生成代码
     */
    private void generatorCode(String tableName, ZipOutputStream zip) {
        // 查询表信息
        GenTable table = genTableMapper.selectGenTableByName(tableName);
        List<Long> menuIds = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            menuIds.add(identifierGenerator.nextId(null).longValue());
        }
        table.setMenuIds(menuIds);
        // 设置子表信息
        setSubTable(table);
        // 设置主键列信息
        setPkColumn(table);
        //初始化 VM 方法
        VelocityInitializer.initVelocity();

        VelocityContext context = VelocityUtils.prepareContext(table);

        // 获取模版列表
        List<String> templates = VelocityUtils.getTemplateList(table.getTplCategory());
        for (String template : templates) {
            // 渲染模版
            StringWriter sw = new StringWriter();
            Template tpl = Velocity.getTemplate(template, Constants.UTF8);
            tpl.merge(context, sw);
            // 添加到 zip
            try {
                zip.putNextEntry(new ZipEntry(VelocityUtils.getFileName(template, table)));
                IoUtil.write(zip, StandardCharsets.UTF_8, false, sw.toString());
                IoUtil.close(sw);
                zip.flush();
                zip.closeEntry();
            } catch (IOException e) {
                log.error("渲染模版失败，表名：" + table.getTableName(), e);
            }
        }

    }

    /**
     * 设置主键列信息
     */
    private void setPkColumn(GenTable table) {
        // 如果有主键，就用主键列
        for (GenTableColumn column : table.getColumns()) {
            if (column.isPk()) {
                table.setPkColumn(column);
                break;
            }
        }
        // 如果没有设置主键，就用第一个字段作为主键
        if (ObjectUtil.isNull(table.getPkColumn())) {
            table.setPkColumn(table.getColumns().get(0));
        }
        // 如果是主子表
        if (GenConstants.TPL_SUB.equals(table.getTplCategory())) {
            // 设置子表的主键
            for (GenTableColumn column : table.getSubTable().getColumns()) {
                if (column.isPk()) {
                    table.getSubTable().setPkColumn(column);
                    break;
                }
            }
            // 如果没有主键，使用第一个字段
            if (ObjectUtil.isNull(table.getSubTable().getPkColumn())) {
                table.getSubTable().setPkColumn(table.getSubTable().getColumns().get(0));
            }
        }
    }

    /**
     * 设置子表信息
     */
    private void setSubTable(GenTable table) {
        String subTableName = table.getSubTableName();
        if (StringUtils.isNotEmpty(subTableName)) {
            table.setSubTable(genTableMapper.selectGenTableByName(subTableName));
        }
    }

    /**
     * 设置代码生成其他选项值
     */
    private void setTableFromOptions(GenTable genTable) {
        Dict paramsObj = JsonUtils.parseMap(genTable.getOptions());
        if (ObjectUtil.isNotNull(paramsObj)) {
            String treeCode = paramsObj.getStr(GenConstants.TREE_CODE);
            String treeParentCode = paramsObj.getStr(GenConstants.TREE_PARENT_CODE);
            String treeName = paramsObj.getStr(GenConstants.TREE_NAME);
            String parentMenuId = paramsObj.getStr(GenConstants.PARENT_MENU_ID);
            String parentMenuName = paramsObj.getStr(GenConstants.PARENT_MENU_NAME);

            genTable.setTreeCode(treeCode);
            genTable.setTreeParentCode(treeParentCode);
            genTable.setTreeName(treeName);
            genTable.setParentMenuId(parentMenuId);
            genTable.setParentMenuName(parentMenuName);
        }
    }

    private QueryWrapper<GenTable> buildGenTableQueryWrapper(GenTable genTable) {
        Map<String, Object> params = genTable.getParams();
        QueryWrapper<GenTable> query = Wrappers.query();
        query.like(StringUtils.isNotBlank(genTable.getTableName()), "lower(table_name)", StringUtils.lowerCase(genTable.getTableName()))
            .like(StringUtils.isNotBlank(genTable.getTableComment()), "lower(table_comment)", StringUtils.lowerCase(genTable.getTableComment()))
            .between(params.get("beginTime") != null && params.get("endTime") != null,
                "create_time", params.get("beginTime"), params.get("endTime"));
        return query;
    }
}
