package com.lemon.system.mapper;

import com.lemon.common.core.mapper.BaseMapperPlus;
import com.lemon.system.domain.SysPost;

import java.util.List;

/**
 * 岗位信息
 */
public interface SysPostMapper extends BaseMapperPlus<SysPostMapper, SysPost, SysPost> {

    /**
     * 查询用户所属岗位组
     *
     * @param userName 用户名
     * @return 结果
     */
    List<SysPost> selectPostsByUserName(String userName);
}
