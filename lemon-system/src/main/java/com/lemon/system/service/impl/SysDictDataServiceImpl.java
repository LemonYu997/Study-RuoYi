package com.lemon.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lemon.common.constant.CacheNames;
import com.lemon.common.core.domain.entity.SysDictData;
import com.lemon.common.core.page.PageQuery;
import com.lemon.common.core.page.TableDataInfo;
import com.lemon.common.exception.ServiceException;
import com.lemon.common.utils.StringUtils;
import com.lemon.common.utils.redis.CacheUtils;
import com.lemon.system.mapper.SysDictDataMapper;
import com.lemon.system.service.ISysDictDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 字典数据
 */
@RequiredArgsConstructor
@Service
public class SysDictDataServiceImpl implements ISysDictDataService {
    private final SysDictDataMapper baseMapper;

    @Override
    public TableDataInfo<SysDictData> selectPageDictDataList(SysDictData dictData, PageQuery pageQuery) {
        LambdaQueryWrapper<SysDictData> lqw = Wrappers.lambdaQuery(SysDictData.class);
        lqw.eq(StringUtils.isNotBlank(dictData.getDictType()), SysDictData::getDictType, dictData.getDictType())
            .like(StringUtils.isNotBlank(dictData.getDictLabel()), SysDictData::getDictLabel, dictData.getDictLabel())
            .eq(StringUtils.isNotBlank(dictData.getStatus()), SysDictData::getStatus, dictData.getStatus())
            .orderByAsc(SysDictData::getDictSort);
        Page<SysDictData> page = baseMapper.selectPage(pageQuery.build(), lqw);
        return TableDataInfo.build(page);
    }

    /**
     * 根据字典数据ID查询信息
     *
     * @param dictCode 字典数据ID
     * @return 字典数据
     */
    @Override
    public SysDictData selectDictDataById(Long dictCode) {
        return baseMapper.selectById(dictCode);
    }

    /**
     * 新增保存字典数据信息
     *
     * @param dictData 字典数据信息
     * @return 结果
     */
    @Override
    @CachePut(cacheNames = CacheNames.SYS_DICT, key = "#data.dictType")
    public List<SysDictData> insertDictData(SysDictData dictData) {
        int row = baseMapper.insert(dictData);
        if (row > 0) {
            // 更新缓存
            return baseMapper.selectDictDataByType(dictData.getDictType());
        }
        throw new ServiceException("操作失败");
    }

    /**
     * 修改保存字典数据信息
     *
     * @param dictData 字典数据信息
     * @return 结果
     */
    @Override
    @CachePut(cacheNames = CacheNames.SYS_DICT, key = "#data.dictType")
    public List<SysDictData> updateDictData(SysDictData dictData) {
        int row = baseMapper.updateById(dictData);
        if (row > 0) {
            return baseMapper.selectDictDataByType(dictData.getDictType());
        }
        throw new ServiceException("操作失败");
    }

    /**
     * 批量删除字典数据信息
     *
     * @param dictCodes 需要删除的字典数据ID
     */
    @Override
    public void deleteDictDataByIds(Long[] dictCodes) {
        for (Long dictCode : dictCodes) {
            SysDictData data = selectDictDataById(dictCode);
            baseMapper.deleteById(dictCode);
            CacheUtils.evict(CacheNames.SYS_DICT, data.getDictType());
        }
    }
}
