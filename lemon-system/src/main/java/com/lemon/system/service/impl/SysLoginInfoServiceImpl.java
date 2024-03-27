package com.lemon.system.service.impl;

import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lemon.common.constant.Constants;
import com.lemon.common.core.domain.event.LoginInfoEvent;
import com.lemon.common.core.page.PageQuery;
import com.lemon.common.core.page.TableDataInfo;
import com.lemon.common.utils.ServletUtils;
import com.lemon.common.utils.StringUtils;
import com.lemon.common.utils.ip.AddressUtils;
import com.lemon.system.domain.SysLoginInfo;
import com.lemon.system.mapper.SysLoginInfoMapper;
import com.lemon.system.service.ISysLoginInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

/**
 * 系统访问日志情况信息
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class SysLoginInfoServiceImpl implements ISysLoginInfoService {
    private final SysLoginInfoMapper sysLoginInfoMapper;

    /**
     * 处理登录事件，记录登录日志
     */
    @Async  //需要开启
    @EventListener      //监听登录事件 由 SysLoginService 传递
    public void recordLoginInfo(LoginInfoEvent loginInfoEvent) {
        HttpServletRequest request = loginInfoEvent.getRequest();
        //获取客户端信息和IP信息
        UserAgent userAgent = UserAgentUtil.parse(request.getHeader(HttpHeaders.USER_AGENT));
        String ip = ServletUtils.getClientIP(request);

        //获取地址 使用ip2region
        String address = AddressUtils.getRealAddressByIP(ip);

        //整理日志输出信息
        StringBuilder sb = new StringBuilder();
        sb.append(getBlock(ip));
        sb.append(address);
        sb.append(getBlock(loginInfoEvent.getUsername()));
        sb.append(getBlock(loginInfoEvent.getStatus()));
        sb.append(getBlock(loginInfoEvent.getMessage()));
        //打印信息到日志
        log.info(sb.toString(), loginInfoEvent.getArgs());

        //获取客户端操作系统
        String os = userAgent.getOs().getName();
        //获取客户端浏览器
        String browser = userAgent.getBrowser().getName();

        //封装对象
        SysLoginInfo sysLoginInfo = new SysLoginInfo();
        sysLoginInfo.setUserName(loginInfoEvent.getUsername());
        sysLoginInfo.setIpaddr(ip);
        sysLoginInfo.setLoginLocation(address);
        sysLoginInfo.setBrowser(browser);
        sysLoginInfo.setOs(os);
        sysLoginInfo.setMsg(loginInfoEvent.getMessage());

        //日志状态
        if (StringUtils.equalsAny(loginInfoEvent.getStatus(), Constants.LOGIN_SUCCESS, Constants.LOGOUT, Constants.REGISTER)) {
            sysLoginInfo.setStatus(Constants.SUCCESS);
        } else {
            sysLoginInfo.setStatus(Constants.FAIL);
        }

        //插入数据
        insertLoginInfo(sysLoginInfo);
    }

    /**
     * 新增系统登录日志
     *
     * @param sysLoginInfo 访问日志对象
     */
    @Override
    public void insertLoginInfo(SysLoginInfo sysLoginInfo) {
        sysLoginInfo.setLoginTime(new Date());
        sysLoginInfoMapper.insert(sysLoginInfo);
    }

    /**
     * 获取系统访问记录列表
     */
    @Override
    public TableDataInfo<SysLoginInfo> selectPageLoginInfoList(SysLoginInfo loginInfo, PageQuery pageQuery) {
        Map<String, Object> params = loginInfo.getParams();
        LambdaQueryWrapper<SysLoginInfo> lqw = Wrappers.lambdaQuery(SysLoginInfo.class);
        lqw.like(StringUtils.isNotBlank(loginInfo.getIpaddr()), SysLoginInfo::getIpaddr, loginInfo.getIpaddr())
            .eq(StringUtils.isNotBlank(loginInfo.getStatus()), SysLoginInfo::getStatus, loginInfo.getStatus())
            .like(StringUtils.isNotBlank(loginInfo.getUserName()), SysLoginInfo::getUserName, loginInfo.getUserName())
            .between(params.get("beginTime") != null && params.get("endTime") != null,
                SysLoginInfo::getLoginTime, params.get("beginTime"), params.get("endTime"));
        // 默认排序
        if (StringUtils.isBlank(pageQuery.getOrderByColumn())) {
            pageQuery.setOrderByColumn("info_id");
            pageQuery.setIsAsc("desc");
        }
        Page<SysLoginInfo> page = sysLoginInfoMapper.selectPage(pageQuery.build(), lqw);
        return TableDataInfo.build(page);
    }

    /**
     * 清空系统访问记录
     */
    @Override
    public void cleanLoginInfo() {
        sysLoginInfoMapper.delete(Wrappers.lambdaQuery());
    }

    /**
     * 批量删除系统登录日志
     */
    @Override
    public int deleteLoginInfoByIds(Long[] infoIds) {
        return sysLoginInfoMapper.deleteBatchIds(Arrays.asList(infoIds));
    }

    /**
     * 格式化
     */
    private String getBlock(Object msg) {
        if (msg == null) {
            msg = "";
        }
        return "[" + msg.toString() + "]";
    }
}
