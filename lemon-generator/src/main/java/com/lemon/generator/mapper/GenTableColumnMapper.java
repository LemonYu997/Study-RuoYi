package com.lemon.generator.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.lemon.common.core.mapper.BaseMapperPlus;
import com.lemon.generator.domain.GenTableColumn;

import java.util.List;

/**
 * 数据层
 * 屏蔽数据权限注解
 */
@InterceptorIgnore(dataPermission = "true")
public interface GenTableColumnMapper extends BaseMapperPlus<GenTableColumnMapper, GenTableColumn, GenTableColumn> {
    /**
     * 根据表明查询列信息
     */
    List<GenTableColumn> selectDbTableColumnsByName(String tableName);
}
