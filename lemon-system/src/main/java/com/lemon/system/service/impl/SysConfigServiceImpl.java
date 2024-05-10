package com.lemon.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lemon.common.constant.CacheNames;
import com.lemon.common.constant.UserConstants;
import com.lemon.common.core.page.PageQuery;
import com.lemon.common.core.page.TableDataInfo;
import com.lemon.common.exception.ServiceException;
import com.lemon.common.utils.StringUtils;
import com.lemon.common.utils.redis.CacheUtils;
import com.lemon.system.domain.SysConfig;
import com.lemon.system.mapper.SysConfigMapper;
import com.lemon.system.service.ISysConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 参数配置 服务层实现
 */
@Service
@RequiredArgsConstructor
public class SysConfigServiceImpl implements ISysConfigService {
    private final SysConfigMapper sysConfigMapper;

    /**
     * 分页获取参数配置列表
     */
    @Override
    public TableDataInfo<SysConfig> selectPageConfigList(SysConfig config, PageQuery pageQuery) {
        Map<String, Object> params = config.getParams();
        LambdaQueryWrapper<SysConfig> lqw = new LambdaQueryWrapper<SysConfig>()
            .like(StringUtils.isNotBlank(config.getConfigName()), SysConfig::getConfigName, config.getConfigName())
            .eq(StringUtils.isNotBlank(config.getConfigType()), SysConfig::getConfigType, config.getConfigType())
            .like(StringUtils.isNotBlank(config.getConfigKey()), SysConfig::getConfigKey, config.getConfigKey())
            .between(params.get("beginTime") != null && params.get("endTime") != null,
                SysConfig::getCreateTime, params.get("beginTime"), params.get("endTime"));
        Page<SysConfig> page = sysConfigMapper.selectPage(pageQuery.build(), lqw);
        return TableDataInfo.build(page);
    }

    /**
     * 查询参数配置列表
     */
    public List<SysConfig> selectConfigList(SysConfig config) {
        Map<String, Object> params = config.getParams();
        LambdaQueryWrapper<SysConfig> lqw = Wrappers.<SysConfig>lambdaQuery();
        lqw.like(StringUtils.isNotBlank(config.getConfigName()), SysConfig::getConfigName, config.getConfigName())
            .eq(StringUtils.isNotBlank(config.getConfigType()), SysConfig::getConfigType, config.getConfigType())
            .like(StringUtils.isNotBlank(config.getConfigKey()), SysConfig::getConfigKey, config.getConfigKey())
            .between(params.get("beginTime") != null && params.get("endTime") != null,
                SysConfig::getCreateTime, params.get("beginTime"), params.get("endTime"));
        return sysConfigMapper.selectList(lqw);
    }

    /**
     * 根据参数编号获取详细信息
     */
    @Override
    public SysConfig selectConfigById(Long configId) {
        return sysConfigMapper.selectById(configId);
    }

    /**
     * 根据参数键名查询参数值
     */
    @Override
    public String selectConfigByKey(SysConfig configKey) {
        SysConfig retConfig = sysConfigMapper.selectOne(Wrappers.<SysConfig>lambdaQuery()
            .eq(SysConfig::getConfigKey, configKey));
        if (ObjectUtil.isNotNull(retConfig)) {
            return retConfig.getConfigValue();
        }

        return StringUtils.EMPTY;
    }

    /**
     * 校验参数键名是否唯一
     */
    @Override
    public boolean checkConfigKeyUnique(SysConfig config) {
        long configId = ObjectUtil.isNotNull(config.getConfigId()) ? -1L : config.getConfigId();
        SysConfig info = sysConfigMapper.selectOne(Wrappers.<SysConfig>lambdaQuery().eq(SysConfig::getConfigKey, config.getConfigKey()));
        if (ObjectUtil.isNotNull(info) && info.getConfigId() != configId) {
            return false;
        }
        return true;
    }

    /**
     * 新增参数配置
     */
    @Override
    @CachePut(cacheNames = CacheNames.SYS_CONFIG, key = "#config.configKey")
    public String insertConfig(SysConfig config) {
        int row = sysConfigMapper.insert(config);
        if (row > 0) {
            return config.getConfigValue();
        }
        throw new SecurityException("操作失败");
    }

    /**
     * 修改参数配置
     */
    @Override
    @CachePut(cacheNames = CacheNames.SYS_CONFIG, key = "#config.configKey")
    public String updateConfig(SysConfig config) {
        int row = 0;
        if (config.getConfigId() != null) {
            SysConfig temp = sysConfigMapper.selectById(config.getConfigId());
            if (!StringUtils.equals(temp.getConfigKey(), config.getConfigKey())) {
                CacheUtils.evict(CacheNames.SYS_CONFIG, temp.getConfigKey());
            }
            row = sysConfigMapper.updateById(config);
        } else {
            row = sysConfigMapper.update(config, Wrappers.<SysConfig>lambdaQuery()
                .eq(SysConfig::getConfigKey, config.getConfigKey()));
        }
        if (row > 0) {
            return config.getConfigValue();
        }

        throw new SecurityException("操作失败");
    }

    /**
     * 批量删除参数配置
     */
    @Override
    public void deleteConfigByIds(Long[] configIds) {
        for (Long configId : configIds) {
            SysConfig config = selectConfigById(configId);
            if (StringUtils.equals(UserConstants.YES, config.getConfigType())) {
                throw new ServiceException(String.format("内置参数【%1$s】不能删除", config.getConfigKey()));
            }
            //从缓存中剔除
            CacheUtils.evict(CacheNames.SYS_CONFIG, config.getConfigKey());
        }
        sysConfigMapper.deleteBatchIds(Arrays.asList(configIds));
    }

    /**
     * 刷新缓存
     */
    @Override
    public void resetConfigCache() {
        // 清除缓存
        clearConfigCache();
        // 加载参数缓存
        loadingConfigCache();
    }

    /**
     * 加载参数缓存数据
     */
    private void loadingConfigCache() {
        List<SysConfig> sysConfigs = selectConfigList(new SysConfig());
        sysConfigs.forEach(config ->
            CacheUtils.put(CacheNames.SYS_CONFIG, config.getConfigKey(), config.getConfigValue()));
    }

    /**
     * 清空参数缓存数据
     */
    private void clearConfigCache() {
        CacheUtils.clear(CacheNames.SYS_CONFIG);
    }


}
