package com.lemon.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lemon.common.constant.CacheNames;
import com.lemon.common.constant.UserConstants;
import com.lemon.common.core.domain.entity.SysDictData;
import com.lemon.common.core.domain.entity.SysDictType;
import com.lemon.common.core.page.PageQuery;
import com.lemon.common.core.page.TableDataInfo;
import com.lemon.common.core.service.DictService;
import com.lemon.common.exception.ServiceException;
import com.lemon.common.utils.StreamUtils;
import com.lemon.common.utils.StringUtils;
import com.lemon.common.utils.redis.CacheUtils;
import com.lemon.system.mapper.SysDictDataMapper;
import com.lemon.system.mapper.SysDictTypeMapper;
import com.lemon.system.service.ISysDictTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 字典类型
 */
@RequiredArgsConstructor
@Service
public class SysDictTypeServiceImpl implements ISysDictTypeService, DictService {
    private final SysDictTypeMapper baseMapper;
    private final SysDictDataMapper dictDataMapper;

    /**
     * 查询字典类型列表
     */
    @Override
    public TableDataInfo<SysDictType> selectPageDictTypeList(SysDictType dictType, PageQuery pageQuery) {
        Map<String, Object> params = dictType.getParams();
        LambdaQueryWrapper<SysDictType> lqw = Wrappers.lambdaQuery(SysDictType.class);
        lqw.like(StringUtils.isNotBlank(dictType.getDictName()), SysDictType::getDictName, dictType.getDictName())
            .eq(StringUtils.isNotBlank(dictType.getStatus()), SysDictType::getStatus, dictType.getStatus())
            .like(StringUtils.isNotBlank(dictType.getDictType()), SysDictType::getDictType, dictType.getDictType())
            .between(params.get("beginTime") != null && params.get("endTime") != null, SysDictType::getCreateTime, params.get("beginTime"), params.get("endTime"));
        Page<SysDictType> page = baseMapper.selectPage(pageQuery.build(), lqw);
        return TableDataInfo.build(page);
    }

    /**
     * 根据字典类型ID查询信息
     *
     * @param dictId 字典类型ID
     * @return 字典类型
     */
    @Override
    public SysDictType selectDictTypeById(Long dictId) {
        return baseMapper.selectById(dictId);
    }

    /**
     * 新增保存字典类型信息
     *
     * @param dictType @return 结果
     * @return
     */
    @Override
    @CachePut(cacheNames = CacheNames.SYS_DICT, key = "#dict.dictType")
    public List<SysDictData> insertDictType(SysDictType dictType) {
        int row = baseMapper.insert(dictType);
        if (row > 0) {
            // 新增 type 下无 data 数据 返回空防止缓存穿透
            return new ArrayList<>();
        }
        throw new ServiceException("操作失败");
    }

    /**
     * 修改保存字典类型信息
     *
     * @param dictType 字典类型信息
     * @return 结果
     */
    @Override
    @CachePut(cacheNames = CacheNames.SYS_DICT, key = "#dict.dictType")
    @Transactional(rollbackFor = Exception.class)
    public List<SysDictData> updateDictType(SysDictType dictType) {
        SysDictType oldDict = baseMapper.selectById(dictType.getDictId());
        dictDataMapper.update(null, Wrappers.lambdaUpdate(SysDictData.class)
            .set(SysDictData::getDictType, dictType.getDictType())
            .eq(SysDictData::getDictType, oldDict.getDictType()));

        int row = baseMapper.updateById(dictType);
        if (row > 0) {
            CacheUtils.evict(CacheNames.SYS_DICT, oldDict.getDictType());
            return dictDataMapper.selectDictDataByType(dictType.getDictType());
        }
        throw new ServiceException("操作失败");
    }

    /**
     * 批量删除字典信息
     *
     * @param dictIds 需要删除的字典ID
     */
    @Override
    public void deleteDictTypeByIds(Long[] dictIds) {
        for (Long dictId : dictIds) {
            SysDictType dictType = selectDictTypeById(dictId);
            if (dictDataMapper.exists(Wrappers.lambdaQuery(SysDictData.class)
                .eq(SysDictData::getDictType, dictType.getDictType()))) {
                throw new ServiceException(String.format("%1$s已分配,不能删除", dictType.getDictName()));
            }
            CacheUtils.evict(CacheNames.SYS_DICT, dictType.getDictType());
        }
        baseMapper.deleteBatchIds(Arrays.asList(dictIds));
    }

    /**
     * 清空字典缓存数据
     */
    @Override
    public void clearDictCache() {
        CacheUtils.clear(CacheNames.SYS_DICT);
    }

    /**
     * 加载字典缓存数据
     */
    @Override
    public void loadingDictCache() {
        List<SysDictData> dictDataList = dictDataMapper.selectList(Wrappers.lambdaQuery(SysDictData.class)
            .eq(SysDictData::getStatus, UserConstants.DICT_NORMAL));
        Map<String, List<SysDictData>> dictDataMap = StreamUtils.groupByKey(dictDataList, SysDictData::getDictType);
        dictDataMap.forEach((k, v) -> {
            List<SysDictData> dictList = StreamUtils.sorted(v, Comparator.comparing(SysDictData::getDictSort));
            CacheUtils.put(CacheNames.SYS_DICT, k, dictList);
        });
    }

    /**
     * 重置字典缓存数据
     */
    @Override
    public void resetDictCache() {
        clearDictCache();
        loadingDictCache();
    }

    /**
     * 根据所有字典类型
     *
     * @return 字典类型集合信息
     */
    @Override
    public List<SysDictType> selectDictTypeAll() {
        return baseMapper.selectList();
    }

    /**
     * 校验字典类型称是否唯一
     *
     * @param dict@return 结果
     */
    @Override
    public boolean checkDictTypeUnique(SysDictType dict) {
        boolean exist = baseMapper.exists(Wrappers.lambdaQuery(SysDictType.class)
            .eq(SysDictType::getDictType, dict.getDictType())
            .ne(ObjectUtil.isNotNull(dict.getDictId()), SysDictType::getDictId, dict.getDictId()));
        return !exist;
    }

}
