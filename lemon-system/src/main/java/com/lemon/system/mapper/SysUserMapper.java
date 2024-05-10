package com.lemon.system.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lemon.common.core.domain.entity.SysUser;
import com.lemon.common.core.mapper.BaseMapperPlus;
import org.apache.ibatis.annotations.Param;

public interface SysUserMapper extends BaseMapperPlus<SysUserMapper, SysUser, SysUser> {
    /**
     * 通过用户名查询用户
     *
     * @param userName 用户名
     * @return 用户对象信息
     */
    SysUser selectUserByUsername(String userName);

    /**
     * 通过用户ID查询用户
     *
     * @param userId 用户ID
     * @return 用户对象信息
     */
    SysUser selectUserById(Long userId);

    /**
     * 条件分页查询
     * @param page 分页条件
     * @param queryWrapper 查询条件
     */
    Page<SysUser> selectPageUserList(@Param("page") Page<SysUser> page, @Param(Constants.WRAPPER) Wrapper<SysUser> queryWrapper);

    /**
     * 根据条件分页查询已配用户角色列表
     */
    Page<SysUser> selectAllocatedList(@Param("page") Page<Object> build, @Param(Constants.WRAPPER) QueryWrapper<SysUser> qw);

    /**
     * 根据条件分页查询未分配用户角色列表
     */
    Page<SysUser> selectUnallocatedList(Page<Object> build, QueryWrapper<SysUser> wrapper);
}
