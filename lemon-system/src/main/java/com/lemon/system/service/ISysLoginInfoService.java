package com.lemon.system.service;

import com.lemon.system.domain.SysLoginInfo;

/**
 * 系统登录日志 服务层
 */
public interface ISysLoginInfoService {
    /**
     * 新增系统登录日志
     *
     * @param sysLoginInfo 访问日志对象
     */
    public void insertLoginInfo(SysLoginInfo sysLoginInfo);
}
