package com.lemon.common.core.domain.model;

import com.lemon.common.core.domain.dto.RoleDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 登录用户身份权限
 */
@Data
@NoArgsConstructor
public class LoginUser implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 部门id
     */
    private Long deptId;

    /**
     * 部门名
     */
    private String deptName;

    /**
     * 用户唯一标识
     */
    private String token;

    /**
     * 用户类型
     */
    private String userType;

    /**
     * 登录时间
     */
    private Long loginTime;

    /**
     * 过期时间
     */
    private Long expireTime;

    /**
     * 登录IP地址
     */
    private String ipaddr;

    /**
     * 登录地点
     */
    private String loginLocation;

    /**
     * 浏览器类型
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 角色对象
     */
    private List<RoleDTO> roles;

    /**
     * 数据权限 当前角色ID
     */
    private Long roleId;
}
