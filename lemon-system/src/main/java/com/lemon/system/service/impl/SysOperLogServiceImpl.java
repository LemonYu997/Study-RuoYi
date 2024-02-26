package com.lemon.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ArrayUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lemon.common.core.domain.event.OperLogEvent;
import com.lemon.common.core.page.PageQuery;
import com.lemon.common.core.page.TableDataInfo;
import com.lemon.common.utils.StringUtils;
import com.lemon.common.utils.ip.AddressUtils;
import com.lemon.system.domain.SysOperLog;
import com.lemon.system.mapper.SysOperLogMapper;
import com.lemon.system.service.ISysOperLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;

/**
 * 操作日志 服务层
 */
@Service
@RequiredArgsConstructor
public class SysOperLogServiceImpl implements ISysOperLogService {

    private final SysOperLogMapper logMapper;

    /**
     * 操作日志事件处理
     * 通过 Controller 方法上 @Log 注解，经过 AOP 切片处理发送事件，在这里进行处理
     * @param logEvent 操作日志事件
     */
    @Async
    @EventListener
    public void recordOperLog(OperLogEvent logEvent) {
        SysOperLog operLog = BeanUtil.toBean(logEvent, SysOperLog.class);
        // 远程查询操作地点
        operLog.setOperLocation(AddressUtils.getRealAddressByIP(operLog.getOperIp()));
        // 入库
        insertOperLog(operLog);
    }

    /**
     * 新增操作日志
     */
    @Override
    public void insertOperLog(SysOperLog operLog) {
        operLog.setOperTime(new Date());
        logMapper.insert(operLog);
    }

    /**
     * 操作日志列表
     */
    @Override
    public TableDataInfo<SysOperLog> selectPageOperLogList(SysOperLog operLog, PageQuery pageQuery) {
        Map<String, Object> params = operLog.getParams();
        LambdaQueryWrapper<SysOperLog> lqw = Wrappers.lambdaQuery(SysOperLog.class);
        lqw.like(StringUtils.isNotBlank(operLog.getOperIp()), SysOperLog::getOperIp, operLog.getOperIp());
        lqw.like(StringUtils.isNotBlank(operLog.getTitle()), SysOperLog::getTitle, operLog.getTitle());
        lqw.eq(operLog.getBusinessType() != null && operLog.getBusinessType() > 0,
            SysOperLog::getBusinessType, operLog.getBusinessType());
        lqw.in(ArrayUtil.isNotEmpty(operLog.getBusinessType()),
            SysOperLog::getBusinessType, Arrays.asList(operLog.getBusinessTypes()));
        lqw.eq(operLog.getStatus() != null, SysOperLog::getStatus, operLog.getStatus());
        lqw.like(StringUtils.isNotBlank(operLog.getOperName()), SysOperLog::getOperName, operLog.getOperName());
        lqw.between(params.get("beginTime") != null && params.get("endTime") != null,
            SysOperLog::getOperTime, params.get("beginTime"), params.get("endTime"));

        if (StringUtils.isBlank(pageQuery.getOrderByColumn())) {
            pageQuery.setOrderByColumn("oper_id");
            pageQuery.setIsAsc("desc");
        }

        Page<SysOperLog> page = logMapper.selectPage(pageQuery.build(), lqw);
        return TableDataInfo.build(page);
    }

    /**
     * 批量删除操作日志记录
     */
    @Override
    public int deleteOperLogByIds(Long[] operIds) {
        return logMapper.deleteBatchIds(Arrays.asList(operIds));
    }

    /**
     * 清理操作日志记录
     */
    @Override
    public void cleanOperLog() {
        logMapper.delete(new LambdaQueryWrapper<>());
    }
}
