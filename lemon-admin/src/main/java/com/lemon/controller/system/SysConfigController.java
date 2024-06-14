package com.lemon.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.lemon.common.annotation.Log;
import com.lemon.common.core.controller.BaseController;
import com.lemon.common.core.domain.R;
import com.lemon.common.core.page.PageQuery;
import com.lemon.common.core.page.TableDataInfo;
import com.lemon.common.enums.BusinessType;
import com.lemon.system.domain.SysConfig;
import com.lemon.system.service.ISysConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 参数配置 信息操作处理
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/config")
public class SysConfigController extends BaseController {
    private final ISysConfigService configService;

    /**
     * 获取参数配置列表
     */
    @GetMapping("/list")
    public TableDataInfo<SysConfig> list(SysConfig config, PageQuery pageQuery) {
        return configService.selectPageConfigList(config, pageQuery);
    }

    /**
     * 根据参数编号获取详细信息
     */
    @GetMapping("/{configId}")
    public R<SysConfig> getInfo(@PathVariable Long configId) {
        return R.ok(configService.selectConfigById(configId));
    }

    /**
     * 根据参数键名查询参数值
     */
    @GetMapping("/configKey/{configKey}")
    public R<String> getConfigKey(@PathVariable String configKey) {
        return R.ok(configService.selectConfigByKey(configKey));
    }

    /**
     * 新增参数配置
     */
    @PostMapping
    @Log(title = "参数管理", businessType = BusinessType.INSERT)
    public R<Void> add(@Validated @RequestBody SysConfig config) {
        if (!configService.checkConfigKeyUnique(config)) {
            return R.fail("新增参数'" + config.getConfigName() + "'失败，参数键名已存在");
        }
        configService.insertConfig(config);
        return R.ok();
    }

    /**
     * 修改参数配置
     */
    @PutMapping
    @Log(title = "参数管理", businessType = BusinessType.UPDATE)
    public R<Void> edit(@Validated @RequestBody SysConfig config) {
        if (!configService.checkConfigKeyUnique(config)) {
            return R.fail("新增参数'" + config.getConfigName() + "'失败，参数键名已存在");
        }
        configService.updateConfig(config);
        return R.ok();
    }

    /**
     * 根据参数键名修改参数配置
     */
    @PutMapping("/updateByKey")
    @Log(title = "参数管理", businessType = BusinessType.UPDATE)
    public R<Void> updateByKey(@RequestBody SysConfig config) {
        configService.updateConfig(config);
        return R.ok();
    }

    /**
     * 删除参数配置
     */
    @DeleteMapping("/{configIds}")
    @Log(title = "参数管理", businessType = BusinessType.DELETE)
    public R<Void> remove(@PathVariable Long[] configIds) {
        configService.deleteConfigByIds(configIds);
        return R.ok();
    }

    /**
     * 刷新参数缓存
     */
    @DeleteMapping("/refreshCache")
    @Log(title = "参数管理", businessType = BusinessType.CLEAN)
    public R<Void> refreshCache() {
        configService.resetConfigCache();
        return R.ok();
    }
}
