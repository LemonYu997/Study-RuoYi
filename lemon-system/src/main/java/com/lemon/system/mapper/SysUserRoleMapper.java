package com.lemon.system.mapper;

import com.lemon.common.core.mapper.BaseMapperPlus;
import com.lemon.system.domain.SysUserRole;

import java.util.List;

/**
 * 用户角色关联表
 */
public interface SysUserRoleMapper extends BaseMapperPlus<SysUserRoleMapper, SysUserRole, SysUserRole> {
    List<Long> selectUserIdsByRoleId(Long roleId);
}
