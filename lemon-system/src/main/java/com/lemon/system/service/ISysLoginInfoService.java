package com.lemon.system.service;

import com.lemon.common.core.page.PageQuery;
import com.lemon.common.core.page.TableDataInfo;
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

    /**
     * 获取系统访问记录列表
     */
    TableDataInfo<SysLoginInfo> selectPageLoginInfoList(SysLoginInfo loginInfo, PageQuery pageQuery);

    /**
     * 清空系统访问记录
     */
    void cleanLoginInfo();

    /**
     * 批量删除系统登录日志
     */
    int deleteLoginInfoByIds(Long[] infoIds);
}
