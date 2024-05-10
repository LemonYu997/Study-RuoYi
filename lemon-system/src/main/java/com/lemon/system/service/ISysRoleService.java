package com.lemon.system.service;

import com.lemon.common.core.domain.entity.SysRole;
import com.lemon.common.core.page.PageQuery;
import com.lemon.common.core.page.TableDataInfo;
import com.lemon.system.domain.SysUserRole;

import java.util.List;

/**
 * 角色业务层
 */
public interface ISysRoleService {
    /**
     * 获取角色信息列表
     */
    TableDataInfo<SysRole> selectPageRoleList(SysRole role, PageQuery pageQuery);

    /**
     * 校验角色是否有数据权限
     */
    void checkRoleDataScope(Long roleId);

    /**
     * 根据条件分页查询角色数据
     */
    List<SysRole> selectRoleList(SysRole role);

    /**
     * 通过角色 id 查询角色
     */
    SysRole selectRoleById(Long roleId);

    /**
     * 校验角色是否允许操作
     */
    void checkRoleAllowed(SysRole role);

    /**
     * 校验角色名称是否唯一
     */
    boolean checkRoleNameUnique(SysRole role);

    /**
     * 校验角色权限是否唯一
     */
    boolean checkRoleKeyUnique(SysRole role);

    /**
     * 新增保存角色信息
     */
    int insertRole(SysRole role);

    /**
     * 修改保存角色信息
     */
    int updateRole(SysRole role);

    /**
     * 修改角色状态
     */
    int updateRoleStatus(SysRole role);

    /**
     * 根据角色id统计被分配的用户数
     */
    long countUserRoleByRoleId(Long roleId);

    /**
     * 批量删除角色
     */
    int deleteRoleByIds(Long[] roleIds);

    /**
     * 获取角色选择框列表
     */
    List<SysRole> selectRoleAll();

    /**
     * 取消授权用户角色
     */
    int deleteAuthUser(SysUserRole userRole);

    /**
     * 清退当前该角色登录用户
     */
    void cleanOnlineUserByRole(Long roleId);

    /**
     * 批量取消授权用户角色
     *
     * @param roleId  角色ID
     * @param userIds 需要取消授权的用户数据ID
     * @return 结果
     */
    int deleteAuthUsers(Long roleId, Long[] userIds);

    /**
     * 批量选择授权用户角色
     *
     * @param roleId  角色ID
     * @param userIds 需要删除的用户数据ID
     * @return 结果
     */
    int insertAuthUsers(Long roleId, Long[] userIds);
}
