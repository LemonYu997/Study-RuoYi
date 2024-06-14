package com.lemon.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lemon.common.constant.UserConstants;
import com.lemon.common.core.domain.entity.SysRole;
import com.lemon.common.core.domain.entity.SysUser;
import com.lemon.common.core.page.PageQuery;
import com.lemon.common.core.page.TableDataInfo;
import com.lemon.common.utils.StreamUtils;
import com.lemon.common.utils.StringUtils;
import com.lemon.system.domain.SysPost;
import com.lemon.system.mapper.SysPostMapper;
import com.lemon.system.mapper.SysRoleMapper;
import com.lemon.system.mapper.SysUserMapper;
import com.lemon.system.mapper.SysUserRoleMapper;
import com.lemon.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 用户管理 业务层
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SysUserServiceImpl implements ISysUserService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysPostMapper postMapper;
    private final SysUserRoleMapper userRoleMapper;

    /**
     * 分页查询
     */
    @Override
    public TableDataInfo<SysUser> selectPageUserList(SysUser user, PageQuery pageQuery) {
        Page<SysUser> page = userMapper.selectPageUserList(pageQuery.build(), buildQueryWrapper(user));
        return TableDataInfo.build(page);
    }

    /**
     * 根据条件分页查询已分配用户角色列表
     */
    @Override
    public TableDataInfo<SysUser> selectAllocatedList(SysUser user, PageQuery pageQuery) {
        QueryWrapper<SysUser> qw = Wrappers.query();
        qw.eq("u.del_flag", UserConstants.USER_NORMAL)
            .eq(ObjectUtil.isNotNull(user.getRoleId()), "r.role_id", user.getRoleId())
            .like(StringUtils.isNotBlank(user.getUserName()), "u.user_name", user.getUserName())
            .eq(StringUtils.isNotBlank(user.getStatus()), "u.status", user.getStatus())
            .like(StringUtils.isNotBlank(user.getPhonenumber()), "u.phonenumber", user.getPhonenumber());
        Page<SysUser> page = userMapper.selectAllocatedList(pageQuery.build(), qw);
        return TableDataInfo.build(page);
    }

    /**
     * 根据条件分页查询未分配用户角色列表
     */
    @Override
    public TableDataInfo<SysUser> selectUnallocatedList(SysUser user, PageQuery pageQuery) {
        List<Long> userIds = userRoleMapper.selectUserIdsByRoleId(user.getRoleId());
        QueryWrapper<SysUser> wrapper = Wrappers.query();
        wrapper.eq("u.del_flag", UserConstants.USER_NORMAL)
            .and(w -> w.ne("r.role_id", user.getRoleId()).or().isNull("r.role_id"))
            .notIn(CollUtil.isNotEmpty(userIds), "u.user_id", userIds)
            .like(StringUtils.isNotBlank(user.getUserName()), "u.user_name", user.getUserName())
            .like(StringUtils.isNotBlank(user.getPhonenumber()), "u.phonenumber", user.getPhonenumber());
        Page<SysUser> page = userMapper.selectUnallocatedList(pageQuery.build(), wrapper);
        return TableDataInfo.build(page);
    }

    /**
     * 查询用户所属角色组
     *
     * @param userName 用户名
     * @return 结果
     */
    @Override
    public String selectUserRoleGroup(String userName) {
        List<SysRole> list = roleMapper.selectRolesByUserName(userName);
        if (CollUtil.isEmpty(list)) {
            return StringUtils.EMPTY;
        }
        return StreamUtils.join(list, SysRole::getRoleName);
    }

    /**
     * 查询用户所属岗位组
     *
     * @param userName 用户名
     * @return 结果
     */
    @Override
    public String selectUserPostGroup(String userName) {
        List<SysPost> list = postMapper.selectPostsByUserName(userName);
        if (CollUtil.isEmpty(list)) {
            return StringUtils.EMPTY;
        }
        return StreamUtils.join(list, SysPost::getPostName);
    }

    /**
     * 修改用户基本信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public int updateUserProfile(SysUser user) {
        return userMapper.updateById(user);
    }

    /**
     * 重置用户密码
     *
     * @param userName 用户名
     * @param password 密码
     * @return 结果
     */
    @Override
    public int resetUserPwd(String userName, String password) {
        return userMapper.update(null, Wrappers.lambdaUpdate(SysUser.class)
            .set(SysUser::getPassword, password)
            .eq(SysUser::getUserName, userName));
    }

    /**
     * 注册用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    @Override
    public boolean registerUser(SysUser user) {
        user.setCreateBy(user.getUserName());
        user.setUpdateBy(user.getUserName());
        return userMapper.insert(user) > 0;
    }

    /**
     * 构建条件查询
     */
    private Wrapper<SysUser> buildQueryWrapper(SysUser user) {
        Map<String, Object> params = user.getParams();
        QueryWrapper<SysUser> wrapper = Wrappers.query();
        wrapper.eq("u.del_flag", UserConstants.USER_NORMAL)
            .eq(ObjectUtil.isNotNull(user.getUserId()), "u.user_id", user.getUserId())
            .like(StringUtils.isNotBlank(user.getUserName()), "u.user_name", user.getUserName())
            .eq(StringUtils.isNotBlank(user.getStatus()), "u.status", user.getStatus())
            .like(StringUtils.isNotBlank(user.getPhonenumber()), "u.phonenumber", user.getPhonenumber())
            .between(params.get("beginTime") != null && params.get("endTime") != null,
                "u.create_time", params.get("beginTime"), params.get("endTime"));
        //todo 部门条件

        return wrapper;
    }

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
