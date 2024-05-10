package com.lemon.system.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lemon.common.core.domain.entity.SysRole;
import com.lemon.common.core.mapper.BaseMapperPlus;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色表 数据层
 */
public interface SysRoleMapper extends BaseMapperPlus<SysRoleMapper, SysRole, SysRole> {

    /**
     * 角色信息分页
     */
    Page<SysRole> selectPageRoleList(@Param("page") Page<Object> page, @Param(Constants.WRAPPER) Wrapper<SysRole> buildQueryWrapper);

    /**
     * 根据条件分页查询角色数据
     */
    List<SysRole> selectRoleList(@Param(Constants.WRAPPER) Wrapper<SysRole> buildQueryWrapper);
}
