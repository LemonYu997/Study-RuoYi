package com.lemon.system.service;

import com.lemon.common.core.domain.entity.SysDictData;
import com.lemon.common.core.domain.entity.SysDictType;
import com.lemon.common.core.page.PageQuery;
import com.lemon.common.core.page.TableDataInfo;

import java.util.List;

public interface ISysDictTypeService {
    /**
     * 查询字典类型列表
     */
    TableDataInfo<SysDictType> selectPageDictTypeList(SysDictType dictType, PageQuery pageQuery);

    /**
     * 根据字典类型ID查询信息
     *
     * @param dictId 字典类型ID
     * @return 字典类型
     */
    SysDictType selectDictTypeById(Long dictId);

    /**
     * 新增保存字典类型信息
     *
     * @param dictType 字典类型信息
     * @return 结果
     */
    List<SysDictData> insertDictType(SysDictType dictType);

    /**
     * 修改保存字典类型信息
     *
     * @param dictType 字典类型信息
     * @return 结果
     */
    List<SysDictData> updateDictType(SysDictType dictType);

    /**
     * 批量删除字典信息
     *
     * @param dictIds 需要删除的字典ID
     */
    void deleteDictTypeByIds(Long[] dictIds);

    /**
     * 清空字典缓存数据
     */
    void clearDictCache();

    /**
     * 加载字典缓存数据
     */
    void loadingDictCache();

    /**
     * 重置字典缓存数据
     */
    void resetDictCache();

    /**
     * 根据所有字典类型
     *
     * @return 字典类型集合信息
     */
    List<SysDictType> selectDictTypeAll();

    /**
     * 根据字典类型查询字典数据
     *
     * @param dictType 字典类型
     * @return 字典数据集合信息
     */
    List<SysDictData> selectDictDataByType(String dictType);

    /**
     * 校验字典类型称是否唯一
     *
     * @param dictType 字典类型
     * @return 结果
     */
    boolean checkDictTypeUnique(SysDictType dictType);
}
