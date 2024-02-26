package com.lemon.system.service;

import com.lemon.common.core.page.PageQuery;
import com.lemon.common.core.page.TableDataInfo;
import com.lemon.system.domain.SysOperLog;

/**
 * 操作日志 服务层
 */
public interface ISysOperLogService {
    /**
     * 新增操作日志
     */
    void insertOperLog(SysOperLog operLog);

    /**
     * 操作日志列表
     */
    TableDataInfo<SysOperLog> selectPageOperLogList(SysOperLog operLog, PageQuery pageQuery);

    /**
     * 批量删除操作日志记录
     */
    int deleteOperLogByIds(Long[] operIds);

    /**
     * 清理操作日志记录
     */
    void cleanOperLog();
}
