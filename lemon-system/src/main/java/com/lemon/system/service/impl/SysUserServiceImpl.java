package com.lemon.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lemon.common.core.domain.entity.SysUser;
import com.lemon.system.mapper.SysUserMapper;
import com.lemon.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * 用户管理 业务层
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SysUserServiceImpl implements ISysUserService {

    private final SysUserMapper userMapper;

    /**
     * 根据id查询用户信息
     *
     * @param userId 用户id
     * @return 用户信息
     */
    @Override
    public SysUser selectUserById(Long userId) {
        return userMapper.selectUserById(userId);
    }

    /**
     * 新增用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)   //开启事务
    public int insertUser(SysUser user) {
        // 新增用户信息
        int rows = userMapper.insert(user);
        return rows;
    }

    /**
     * 修改用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public int updateUser(SysUser user) {
        return userMapper.updateById(user);
    }

    /**
     * 批量删除用户信息
     *
     * @param userIds 需要删除的用户ID
     * @return 结果
     */
    @Override
    public int deleteUserByIds(Long[] userIds) {
        List<Long> list = Arrays.asList(userIds);
        return userMapper.deleteBatchIds(list);
    }

    /**
     * 校验用户名称是否唯一
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public boolean checkUserNameUnique(SysUser user) {
        //查询是否存在同名用户，如果传入ID，查询除去该ID的同名用户（方便其他地方调用）
        boolean exists = userMapper.exists(new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getUserName, user.getUserName())
            .ne(ObjectUtil.isNotNull(user.getUserId()), SysUser::getUserId, user.getUserId()));
        return !exists;
    }

    /**
     * 校验用户手机号是否唯一
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public boolean checkPhoneUnique(SysUser user) {
        boolean exists = userMapper.exists(new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getPhonenumber, user.getPhonenumber())
            .ne(ObjectUtil.isNotNull(user.getUserId()), SysUser::getUserId, user.getUserId()));
        return !exists;
    }

    /**
     * 校验用户邮箱是否唯一
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public boolean checkEmailUnique(SysUser user) {
        boolean exists = userMapper.exists(new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getEmail, user.getEmail())
            .ne(ObjectUtil.isNotNull(user.getUserId()), SysUser::getUserId, user.getUserId()));
        return !exists;
    }

}
