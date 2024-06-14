package com.lemon.system.service;

import com.lemon.common.core.domain.entity.SysUser;
import com.lemon.common.core.page.PageQuery;
import com.lemon.common.core.page.TableDataInfo;

public interface ISysUserService {
    /**
     * 校验用户名称是否唯一
     *
     * @param user 用户信息
     * @return 结果
     */
    boolean checkUserNameUnique(SysUser user);

    /**
     * 校验用户手机号是否唯一
     *
     * @param user 用户信息
     * @return 结果
     */
    boolean checkPhoneUnique(SysUser user);

    /**
     * 校验用户邮箱是否唯一
     *
     * @param user 用户信息
     * @return 结果
     */
    boolean checkEmailUnique(SysUser user);

    /**
     * 根据id查询用户信息
     *
     * @param userId 用户id
     * @return 用户信息
     */
    SysUser selectUserById(Long userId);

    /**
     * 新增用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    int insertUser(SysUser user);

    /**
     * 修改用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    int updateUser(SysUser user);

    /**
     * 批量删除用户信息
     *
     * @param userIds 需要删除的用户ID
     * @return 结果
     */
    int deleteUserByIds(Long[] userIds);

    /**
     * 分页查询
     */
    TableDataInfo<SysUser> selectPageUserList(SysUser user, PageQuery pageQuery);

    /**
     * 根据条件分页查询已分配用户角色列表
     */
    TableDataInfo<SysUser> selectAllocatedList(SysUser user, PageQuery pageQuery);

    /**
     * 根据条件分页查询未分配用户角色列表
     */
    TableDataInfo<SysUser> selectUnallocatedList(SysUser user, PageQuery pageQuery);

    /**
     * 查询用户所属角色组
     *
     * @param userName 用户名
     * @return 结果
     */
    String selectUserRoleGroup(String userName);

    /**
     * 根据用户ID查询用户所属岗位组
     *
     * @param userName 用户名
     * @return 结果
     */
    String selectUserPostGroup(String userName);

    /**
     * 修改用户基本信息
     *
     * @param user 用户信息
     * @return 结果
     */
    int updateUserProfile(SysUser user);

    /**
     * 重置用户密码
     *
     * @param userName 用户名
     * @param password 密码
     * @return 结果
     */
    int resetUserPwd(String userName, String password);

    /**
     * 注册用户信息
     *
     * @param user 用户信息
     * @return 结果
     */
    boolean registerUser(SysUser user);
}
