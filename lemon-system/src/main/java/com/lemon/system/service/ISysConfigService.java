package com.lemon.system.service;

import com.lemon.common.core.page.PageQuery;
import com.lemon.common.core.page.TableDataInfo;
import com.lemon.system.domain.SysConfig;

public interface ISysConfigService {
    /**
     * 获取参数配置列表
     */
    TableDataInfo<SysConfig> selectPageConfigList(SysConfig config, PageQuery pageQuery);

    /**
     * 根据参数编号获取详细信息
     */
    SysConfig selectConfigById(Long configId);

    /**
     * 根据参数键名查询参数值
     */
    String selectConfigByKey(SysConfig configKey);

    /**
     * 校验参数键名是否唯一
     */
    boolean checkConfigKeyUnique(SysConfig config);

    /**
     * 新增参数配置
     */
    String insertConfig(SysConfig config);

    /**
     * 修改参数配置
     */
    String updateConfig(SysConfig config);

    /**
     * 批量删除参数配置
     */
    void deleteConfigByIds(Long[] configIds);

    /**
     * 刷新缓存
     */
    void resetConfigCache();

}
