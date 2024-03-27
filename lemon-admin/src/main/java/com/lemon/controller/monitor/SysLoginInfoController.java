package com.lemon.controller.monitor;

import com.lemon.common.annotation.Log;
import com.lemon.common.constant.CacheConstants;
import com.lemon.common.core.controller.BaseController;
import com.lemon.common.core.domain.R;
import com.lemon.common.core.page.PageQuery;
import com.lemon.common.core.page.TableDataInfo;
import com.lemon.common.enums.BusinessType;
import com.lemon.common.utils.redis.RedisUtils;
import com.lemon.system.domain.SysLoginInfo;
import com.lemon.system.service.ISysLoginInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 系统访问记录
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/monitor/logininfo")
public class SysLoginInfoController extends BaseController {
    private final ISysLoginInfoService loginInfoService;

    /**
     * 获取系统访问记录列表
     */
    @GetMapping("/list")
    public TableDataInfo<SysLoginInfo> list(SysLoginInfo loginInfo, PageQuery pageQuery) {
        return loginInfoService.selectPageLoginInfoList(loginInfo, pageQuery);
    }

    /**
     * 批量删除登录日志
     */
    @Log(title = "登录日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{infoIds}")
    public R<Void> remove(@PathVariable Long[] infoIds) {
        return toAjax(loginInfoService.deleteLoginInfoByIds(infoIds));
    }

    /**
     * 清理系统访问记录
     */
    @Log(title = "登录日志", businessType = BusinessType.CLEAN)
    @DeleteMapping("/clean")
    public R<Void> clean() {
        loginInfoService.cleanLoginInfo();
        return R.ok();
    }

    /**
     * 账户解锁
     */
    @Log(title = "账户解锁", businessType = BusinessType.OTHER)
    @GetMapping("/unlock/{userName}")
    public R<Void> unlock(@PathVariable("userName") String userName) {
        String loginName = CacheConstants.PWD_ERR_CNT_KEY + userName;
        if (RedisUtils.hasKey(loginName)) {
            RedisUtils.deleteObject(loginName);
        }
        return R.ok();
    }
}
