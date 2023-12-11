package com.lemon.system.mapper;

import com.lemon.common.core.domain.entity.SysUser;
import com.lemon.common.core.mapper.BaseMapperPlus;

public interface SysUserMapper extends BaseMapperPlus<SysUserMapper, SysUser, SysUser> {
    /**
     * 通过用户名查询用户
     *
     * @param userName 用户名
     * @return 用户对象信息
     */
    SysUser selectUserByUsername(String userName);
}
