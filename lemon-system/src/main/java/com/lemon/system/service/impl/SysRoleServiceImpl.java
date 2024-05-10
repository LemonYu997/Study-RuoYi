package com.lemon.system.service.impl;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lemon.common.constant.UserConstants;
import com.lemon.common.core.domain.R;
import com.lemon.common.core.domain.entity.SysRole;
import com.lemon.common.core.domain.model.LoginUser;
import com.lemon.common.core.page.PageQuery;
import com.lemon.common.core.page.TableDataInfo;
import com.lemon.common.exception.ServiceException;
import com.lemon.common.helper.LoginHelper;
import com.lemon.common.utils.StreamUtils;
import com.lemon.common.utils.StringUtils;
import com.lemon.system.domain.SysUserRole;
import com.lemon.system.mapper.SysRoleMapper;
import com.lemon.system.mapper.SysUserRoleMapper;
import com.lemon.system.service.ISysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 角色 业务层处理
 */
@RequiredArgsConstructor
@Service
public class SysRoleServiceImpl implements ISysRoleService {
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;

    /**
     * 获取角色信息列表
     */
    @Override
    public TableDataInfo<SysRole> selectPageRoleList(SysRole role, PageQuery pageQuery) {
        Page<SysRole> page = roleMapper.selectPageRoleList(pageQuery.build(), buildQueryWrapper(role));
        return TableDataInfo.build(page);
    }

    /**
     * 校验角色是否有数据权限
     */
    @Override
    public void checkRoleDataScope(Long roleId) {
        // 如果是管理员可以直接跳过校验
        if (!LoginHelper.isAdmin()) {
            SysRole role = new SysRole();
            role.setRoleId(roleId);
            List<SysRole> roles = selectRoleList(role);
            if (CollectionUtils.isNotEmpty(roles)) {
                throw new ServiceException("没有权限访问角色数据！");
            }
        }
    }

    /**
     * 根据条件分页查询角色数据
     */
    @Override
    public List<SysRole> selectRoleList(SysRole role) {
        return roleMapper.selectRoleList(buildQueryWrapper(role));
    }

    /**
     * 通过角色 id 查询角色
     */
    @Override
    public SysRole selectRoleById(Long roleId) {
        return roleMapper.selectById(roleId);
    }

    /**
     * 校验角色是否允许操作
     */
    @Override
    public void checkRoleAllowed(SysRole role) {
        if (ObjectUtil.isNotNull(role.getRoleId()) && role.isAdmin()) {
            throw new ServiceException("不允许操作管理员角色");
        }
        // 新增不允许使用 管理员标识符
        if (ObjectUtil.isNull(role.getRoleId()) && StringUtils.equals(role.getRoleKey(), UserConstants.ADMIN_ROLE_KEY)) {
            throw new ServiceException("不允许使用系统内置管理员角色标识符！");
        }
        // 修改不允许修改 管理员标识符
        if (ObjectUtil.isNotNull(role.getRoleId())) {
            SysRole sysRole = roleMapper.selectById(role.getRoleId());
            // 如果标识符不相等 判断为修改了管理员标识符
            if (!StringUtils.equals(sysRole.getRoleKey(), role.getRoleKey())) {
                if (StringUtils.equals(sysRole.getRoleKey(), UserConstants.ADMIN_ROLE_KEY)) {
                    throw new ServiceException("不允许修改系统内置管理员角色标识符!");
                } else if (StringUtils.equals(role.getRoleKey(), UserConstants.ADMIN_ROLE_KEY)) {
                    throw new ServiceException("不允许使用系统内置管理员角色标识符!");
                }
            }
        }
    }

    /**
     * 校验角色名称是否唯一
     */
    @Override
    public boolean checkRoleNameUnique(SysRole role) {
        boolean exists = roleMapper.exists(Wrappers.lambdaQuery(SysRole.class)
            .eq(SysRole::getRoleName, role.getRoleName())
            .ne(ObjectUtil.isNotNull(role.getRoleId()), SysRole::getRoleId, role.getRoleId()));
        return !exists;
    }

    /**
     * 校验角色权限是否唯一
     */
    @Override
    public boolean checkRoleKeyUnique(SysRole role) {
        boolean exist = roleMapper.exists(Wrappers.lambdaQuery(SysRole.class)
            .eq(SysRole::getRoleKey, role.getRoleKey())
            .ne(ObjectUtil.isNotNull(role.getRoleId()), SysRole::getRoleId, role.getRoleId()));
        return !exist;
    }

    /**
     * 新增保存角色信息
     */
    @Override
    public int insertRole(SysRole role) {
        // 新增角色信息
        return roleMapper.insert(role);
    }

    /**
     * 修改保存角色信息
     */
    @Override
    public int updateRole(SysRole role) {
        return roleMapper.updateById(role);
    }

    /**
     * 修改角色状态
     */
    @Override
    public int updateRoleStatus(SysRole role) {
        // 已分配给用户的角色不能禁用
        if (UserConstants.ROLE_DISABLE.equals(role.getStatus()) &&
            countUserRoleByRoleId(role.getRoleId()) > 0) {
            throw new ServiceException("角色已分配，不能禁用!");
        }
        return roleMapper.updateById(role);
    }

    /**
     * 根据角色id统计被分配的用户数
     */
    @Override
    public long countUserRoleByRoleId(Long roleId) {
        return userRoleMapper.selectCount(Wrappers.lambdaQuery(SysUserRole.class).eq(SysUserRole::getRoleId, roleId));
    }

    /**
     * 批量删除角色
     */
    @Override
    public int deleteRoleByIds(Long[] roleIds) {
        for (Long roleId : roleIds) {
            SysRole sysRole = selectRoleById(roleId);
            checkRoleAllowed(sysRole);
            checkRoleDataScope(roleId);
            if (countUserRoleByRoleId(roleId) > 0) {
                throw new ServiceException(String.format("%1$s已分配，不能删除!", sysRole.getRoleName()));
            }
        }
        List<Long> ids = Arrays.asList(roleIds);

        return roleMapper.deleteBatchIds(ids);
    }

    /**
     * 获取角色选择框列表
     */
    @Override
    public List<SysRole> selectRoleAll() {
        return selectRoleList(new SysRole());
    }

    /**
     * 取消授权用户角色
     */
    @Override
    public int deleteAuthUser(SysUserRole userRole) {
        int rows = userRoleMapper.delete(Wrappers.lambdaQuery(SysUserRole.class)
            .eq(SysUserRole::getRoleId, userRole.getRoleId())
            .eq(SysUserRole::getUserId, userRole.getUserId()));
        if (rows > 0) {
            cleanOnlineUserByRole(userRole.getRoleId());
        }
        return rows;
    }

    /**
     * 清退当前该角色登录用户
     */
    @Override
    public void cleanOnlineUserByRole(Long roleId) {
        // 如果角色未绑定用户，直接返回
        Long num = userRoleMapper.selectCount(Wrappers.lambdaQuery(SysUserRole.class)
            .eq(SysUserRole::getRoleId, roleId));
        if (num == 0) {
            return;
        }
        List<String> keys = StpUtil.searchTokenValue("", 0, -1, false);
        if (CollUtil.isEmpty(keys)) {
            return;
        }
        // 角色关联的在线用户量过大会导致redis阻塞卡顿 谨慎操作
        keys.parallelStream().forEach(key -> {
            String token = StringUtils.substringAfterLast(key, ":");
            // 如果已经过期则跳过
            if (StpUtil.stpLogic.getTokenActiveTimeoutByToken(token) < -1) {
                return;
            }
            // 根据token获取登录用户
            LoginUser loginUser = LoginHelper.getLoginUser(token);
            if (ObjectUtil.isNotNull(loginUser) && loginUser.getRoles().stream().anyMatch(r -> r.getRoleId().equals(roleId))) {
                try {
                    // 对应用户踢出登录
                    StpUtil.logoutByTokenValue(token);
                } catch (NotLoginException ignored) {

                }
            }
        });
    }

    /**
     * 批量取消授权用户角色
     *
     * @param roleId  角色ID
     * @param userIds 需要取消授权的用户数据ID
     * @return 结果
     */
    @Override
    public int deleteAuthUsers(Long roleId, Long[] userIds) {
        int rows = userRoleMapper.delete(Wrappers.lambdaQuery(SysUserRole.class)
            .eq(SysUserRole::getRoleId, roleId)
            .in(SysUserRole::getUserId, Arrays.asList(userIds)));
        if (rows > 0) {
            cleanOnlineUserByRole(roleId);
        }
        return rows;
    }

    /**
     * 批量选择授权用户角色
     *
     * @param roleId  角色ID
     * @param userIds 需要删除的用户数据ID
     * @return 结果
     */
    @Override
    public int insertAuthUsers(Long roleId, Long[] userIds) {
        // 新增用户与角色管理
        int rows = 1;
        List<SysUserRole> list = StreamUtils.toList(Arrays.asList(userIds), userId -> {
            SysUserRole ur = new SysUserRole();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            return ur;
        });
        if (CollUtil.isNotEmpty(list)) {
            rows = userRoleMapper.insertBatch(list) ? list.size() : 0;
        }
        if (rows > 0) {
            cleanOnlineUserByRole(roleId);
        }
        return rows;
    }

    private Wrapper<SysRole> buildQueryWrapper(SysRole role) {
        Map<String, Object> params = role.getParams();
        QueryWrapper<SysRole> qw = Wrappers.query(SysRole.class);
        qw.eq("r.del_flag", UserConstants.ROLE_NORMAL)
            .eq(ObjectUtil.isNotNull(role.getRoleId()), "r.role_id", role.getRoleId())
            .like(StringUtils.isNotBlank(role.getRoleName()), "r.role_name", role.getRoleName())
            .eq(StringUtils.isNotBlank(role.getStatus()), "r.status", role.getStatus())
            .like(StringUtils.isNotBlank(role.getRoleKey()), "r.role_key", role.getRoleKey())
            .between(params.get("beginTime") != null && params.get("endTime") != null,
                "r.create_time", params.get("beginTime"), params.get("endTime"))
            .orderByAsc("r.role_sort").orderByAsc("r.create_time");
        return qw;
    }

}
