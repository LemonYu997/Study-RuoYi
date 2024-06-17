package com.lemon.system.service;

import com.lemon.common.core.page.PageQuery;
import com.lemon.common.core.page.TableDataInfo;
import com.lemon.system.domain.bo.SysOssConfigBo;
import com.lemon.system.domain.vo.SysOssConfigVo;

import java.util.List;

/**
 * 对象存储配置Service接口
 */
public interface ISysOssConfigService {
    /**
     * 查询列表
     */
    TableDataInfo<SysOssConfigVo> queryPageList(SysOssConfigBo bo, PageQuery pageQuery);

    /**
     * 查询单个
     */
    SysOssConfigVo queryById(Long ossConfigId);

    /**
     * 根据新增业务对象插入对象存储配置
     *
     * @param bo 对象存储配置新增业务对象
     * @return
     */
    Boolean insertByBo(SysOssConfigBo bo);

    /**
     * 根据编辑业务对象修改对象存储配置
     *
     * @param bo 对象存储配置编辑业务对象
     * @return
     */
    Boolean updateByBo(SysOssConfigBo bo);

    /**
     * 校验并删除数据
     *
     * @param ids     主键集合
     * @param isValid 是否校验,true-删除前校验,false-不校验
     * @return
     */
    int deleteWithValidByIds(List<Long> ids, boolean isValid);

    /**
     * 启用停用状态
     */
    int updateOssConfigStatus(SysOssConfigBo bo);
}
