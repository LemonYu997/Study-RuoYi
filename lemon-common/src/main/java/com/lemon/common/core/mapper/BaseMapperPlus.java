package com.lemon.common.core.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.toolkit.Db;

import java.util.Collection;
import java.util.List;

/**
 * 自定义 Mapper 增强
 * @param <M> mapper 泛型
 * @param <T> table 泛型
 * @param <V> vo 泛型
 */
@SuppressWarnings("unchecked")  //忽视
public interface BaseMapperPlus<M, T, V> extends BaseMapper<T> {

    /**
     * 批量插入
     */
    default boolean insertBatch(Collection<T> entityList) {
        return Db.saveBatch(entityList);
    }

    /**
     * 批量插入或更新
     */
    default boolean insertOrUpdateBatch(Collection<T> entityList) {
        return Db.saveOrUpdateBatch(entityList);
    }

    default boolean updateBatchById(Collection<T> entityList) {
        return Db.updateBatchById(entityList);
    }

    default List<T> selectList() {
        return this.selectList(new QueryWrapper<>());
    }
}
