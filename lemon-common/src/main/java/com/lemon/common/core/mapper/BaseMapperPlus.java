package com.lemon.common.core.mapper;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.lemon.common.utils.BeanCopyUtils;

import java.io.Serializable;
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

    default Class<V> currentVoClass() {
        return (Class<V>) ReflectionKit.getSuperClassGenericType(this.getClass(), BaseMapperPlus.class, 2);
    }

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

    default <P extends IPage<V>> P selectVoPage(IPage<T> page, Wrapper<T> wrapper) {
        return selectVoPage(page, wrapper, this.currentVoClass());
    }

    /**
     * 分页查询VO
     */
    default <C, P extends IPage<C>> P selectVoPage(IPage<T> page, Wrapper<T> wrapper, Class<C> voClass) {
        List<T> list = this.selectList(page, wrapper);
        IPage<C> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        if (CollUtil.isEmpty(list)) {
            return (P) voPage;
        }
        voPage.setRecords(BeanCopyUtils.copyList(list, voClass));
        return (P) voPage;
    }

    default V selectVoById(Serializable id) {
        return selectVoById(id, this.currentVoClass());
    }

    /**
     * 根据 ID 查询
     */
    default <C> C selectVoById(Serializable id, Class<C> voClass) {
        T obj = this.selectById(id);
        if (ObjectUtil.isNull(obj)) {
            return null;
        }
        return BeanCopyUtils.copy(obj, voClass);
    }
}
