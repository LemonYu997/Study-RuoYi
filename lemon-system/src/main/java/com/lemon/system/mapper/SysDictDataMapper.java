package com.lemon.system.mapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lemon.common.constant.UserConstants;
import com.lemon.common.core.domain.entity.SysDictData;
import com.lemon.common.core.mapper.BaseMapperPlus;

import java.util.List;

/**
 * 字典数据
 */
public interface SysDictDataMapper extends BaseMapperPlus<SysDictDataMapper, SysDictData, SysDictData> {
    /**
     * 根据字典类型查询字典数据
     */
    default List<SysDictData> selectDictDataByType(String dictType) {
        return selectList(Wrappers.lambdaQuery(SysDictData.class)
            .eq(SysDictData::getStatus, UserConstants.DICT_NORMAL)
            .eq(SysDictData::getDictType, dictType)
            .orderByAsc(SysDictData::getDictSort));
    }
}
