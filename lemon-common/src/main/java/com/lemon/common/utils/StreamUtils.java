package com.lemon.common.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * stream 流工具类
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StreamUtils {
    /**
     * 将collection转化为类型不变的map<br>
     * <B>{@code Collection<V>  ---->  Map<K,V>}</B>
     *
     * @param collection 需要转化的集合
     * @param key        V类型转化为K类型的lambda方法
     * @param <V>        collection中的泛型
     * @param <K>        map中的key类型
     * @return 转化后的map
     */
    public static <V, K> Map<K, V> toIdentityMap(Collection<V> collection, Function<V, K> key) {
        if (CollUtil.isEmpty(collection)) {
            return MapUtil.newHashMap();
        }
        return collection.stream().filter(Objects::nonNull).collect(Collectors.toMap(key, Function.identity(), (l, r) -> l));
    }

    /**
     * 将collection转化为List集合，但是两者的泛型不同<br>
     * <B>{@code Collection<E>  ------>  List<T> } </B>
     *
     * @param collection 需要转化的集合
     * @param function   collection中的泛型转化为list泛型的lambda表达式
     * @param <E>        collection中的泛型
     * @param <T>        List中的泛型
     * @return 转化后的list
     */
    public static <E, T> List<T> toList(Collection<E> collection, Function<E, T> function) {
        if (CollUtil.isEmpty(collection)) {
            return CollUtil.newArrayList();
        }
        return collection
            .stream()
            .map(function)
            .filter(Objects::nonNull)
            // 注意此处不要使用 .toList() 新语法 因为返回的是不可变List 会导致序列化问题
            .collect(Collectors.toList());
    }

    /**
     * 将collection过滤
     *
     * @param collection 需要转化的集合
     * @param function   过滤方法
     * @return 过滤后的list
     */
    public static <E> List<E> filter(Collection<E> collection, Predicate<E> function) {
        if (CollUtil.isEmpty(collection)) {
            return CollUtil.newArrayList();
        }
        // 注意此处不要使用 .toList() 新语法 因为返回的是不可变List 会导致序列化问题
        return collection.stream().filter(function).collect(Collectors.toList());
    }

    /**
     * 将collection拼接
     *
     * @param collection 需要转化的集合
     * @param function   拼接方法
     * @param delimiter  拼接符
     * @return 拼接后的list
     */
    public static <E> String join(Collection<E> collection, Function<E, String> function, CharSequence delimiter) {
        if (CollUtil.isEmpty(collection)) {
            return StringUtils.EMPTY;
        }
        return collection.stream().map(function).filter(Objects::nonNull).collect(Collectors.joining(delimiter));
    }

    /**
     * 将collection拼接
     *
     * @param collection 需要转化的集合
     * @param function   拼接方法
     * @return 拼接后的list
     */
    public static <E> String join(Collection<E> collection, Function<E, String> function) {
        return join(collection, function, StringUtils.SEPARATOR);
    }

    /**
     * 将collection按照规则(比如有相同的班级id)分类成map<br>
     * <B>{@code Collection<E> -------> Map<K,List<E>> } </B>
     *
     * @param collection 需要分类的集合
     * @param key        分类的规则
     * @param <E>        collection中的泛型
     * @param <K>        map中的key类型
     * @return 分类后的map
     */
    public static <E, K> Map<K, List<E>> groupByKey(Collection<E> collection, Function<E, K> key) {
        if (CollUtil.isEmpty(collection)) {
            return MapUtil.newHashMap();
        }
        return collection
            .stream().filter(Objects::nonNull)
            .collect(Collectors.groupingBy(key, LinkedHashMap::new, Collectors.toList()));
    }

    /**
     * 将collection排序
     *
     * @param collection 需要转化的集合
     * @param comparing  排序方法
     * @return 排序后的list
     */
    public static <E> List<E> sorted(Collection<E> collection, Comparator<E> comparing) {
        if (CollUtil.isEmpty(collection)) {
            return CollUtil.newArrayList();
        }
        return collection.stream().filter(Objects::nonNull).sorted(comparing).collect(Collectors.toList());
    }
}
