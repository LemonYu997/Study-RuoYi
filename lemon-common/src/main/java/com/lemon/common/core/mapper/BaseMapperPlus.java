package com.lemon.common.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 自定义 Mapper 增强
 * @param <M> mapper 泛型
 * @param <T> table 泛型
 * @param <V> vo 泛型
 */
@SuppressWarnings("unchecked")  //忽视
public interface BaseMapperPlus<M, T, V> extends BaseMapper<T> {

}
