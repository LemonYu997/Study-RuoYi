package com.lemon.system.service;

import com.lemon.common.core.domain.entity.SysDictData;
import com.lemon.common.core.page.PageQuery;
import com.lemon.common.core.page.TableDataInfo;

import java.util.List;

public interface ISysDictDataService {
    TableDataInfo<SysDictData> selectPageDictDataList(SysDictData dictData, PageQuery pageQuery);

    /**
     * 根据字典数据ID查询信息
     *
     * @param dictCode 字典数据ID
     * @return 字典数据
     */
    SysDictData selectDictDataById(Long dictCode);

    /**
     * 新增保存字典数据信息
     *
     * @param dictData 字典数据信息
     * @return 结果
     */
    List<SysDictData> insertDictData(SysDictData dictData);

    /**
     * 修改保存字典数据信息
     *
     * @param dictData 字典数据信息
     * @return 结果
     */
    List<SysDictData> updateDictData(SysDictData dictData);

    /**
     * 批量删除字典数据信息
     *
     * @param dictCodes 需要删除的字典数据ID
     */
    void deleteDictDataByIds(Long[] dictCodes);
}
