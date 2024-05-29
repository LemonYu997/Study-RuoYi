package com.lemon.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lemon.common.core.page.PageQuery;
import com.lemon.common.core.page.TableDataInfo;
import com.lemon.common.exception.ServiceException;
import com.lemon.common.utils.StringUtils;
import com.lemon.system.domain.SysPost;
import com.lemon.system.domain.SysUserPost;
import com.lemon.system.mapper.SysPostMapper;
import com.lemon.system.mapper.SysUserPostMapper;
import com.lemon.system.service.ISysPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 岗位信息
 */
@RequiredArgsConstructor
@Service
public class SysPostServiceImpl implements ISysPostService {
    private final SysPostMapper baseMapper;
    private final SysUserPostMapper userPostMapper;

    @Override
    public TableDataInfo<SysPost> selectPagePostList(SysPost post, PageQuery pageQuery) {
        LambdaQueryWrapper<SysPost> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(post.getPostCode()), SysPost::getPostCode, post.getPostCode())
            .eq(StringUtils.isNotBlank(post.getStatus()), SysPost::getStatus, post.getStatus())
            .like(StringUtils.isNotBlank(post.getPostName()), SysPost::getPostName, post.getPostName());
        Page<SysPost> page = baseMapper.selectPage(pageQuery.build(), lqw);

        return TableDataInfo.build(page);
    }

    /**
     * 通过岗位ID查询岗位信息
     *
     * @param postId 岗位ID
     * @return 角色对象信息
     */
    @Override
    public SysPost selectPostById(Long postId) {
        return baseMapper.selectById(postId);
    }

    /**
     * 校验岗位名称
     *
     * @param post 岗位信息
     * @return 结果
     */
    @Override
    public boolean checkPostNameUnique(SysPost post) {
        boolean exist = baseMapper.exists(Wrappers.lambdaQuery(SysPost.class)
            .eq(SysPost::getPostName, post.getPostName())
            .ne(ObjectUtil.isNotNull(post.getPostId()), SysPost::getPostId, post.getPostId()));
        return !exist;
    }

    /**
     * 校验岗位编码
     *
     * @param post 岗位信息
     * @return 结果
     */
    @Override
    public boolean checkPostCodeUnique(SysPost post) {
        boolean exist = baseMapper.exists(Wrappers.lambdaQuery(SysPost.class)
            .eq(SysPost::getPostCode, post.getPostCode())
            .ne(ObjectUtil.isNotNull(post.getPostId()), SysPost::getPostId, post.getPostId()));
        return !exist;
    }

    /**
     * 新增保存岗位信息
     *
     * @param post 岗位信息
     * @return 结果
     */
    @Override
    public int insertPost(SysPost post) {
        return baseMapper.insert(post);
    }

    /**
     * 通过岗位ID查询岗位使用数量
     *
     * @param postId 岗位ID
     * @return 结果
     */
    @Override
    public long countUserPostById(Long postId) {
        return userPostMapper.selectCount(Wrappers.lambdaQuery(SysUserPost.class)
        .eq(SysUserPost::getPostId, postId));
    }

    /**
     * 修改保存岗位信息
     *
     * @param post 岗位信息
     * @return 结果
     */
    @Override
    public int updatePost(SysPost post) {
        return baseMapper.updateById(post);
    }

    /**
     * 批量删除岗位信息
     *
     * @param postIds 需要删除的岗位ID
     * @return 结果
     */
    @Override
    public int deletePostByIds(Long[] postIds) {
        for (Long postId : postIds) {
            SysPost post = selectPostById(postId);
            if (countUserPostById(postId) > 0) {
                throw new ServiceException(String.format("%1$s已分配，不能删除!", post.getPostName()));
            }
        }
        return baseMapper.deleteBatchIds(Arrays.asList(postIds));
    }

    /**
     * 查询岗位信息集合
     *
     * @param post 岗位信息
     * @return 岗位列表
     */
    @Override
    public List<SysPost> selectPostList(SysPost post) {
        return baseMapper.selectList(Wrappers.lambdaQuery(SysPost.class)
            .like(StringUtils.isNotBlank(post.getPostCode()), SysPost::getPostCode, post.getPostCode())
            .eq(StringUtils.isNotBlank(post.getStatus()), SysPost::getStatus, post.getStatus())
            .like(StringUtils.isNotBlank(post.getPostName()), SysPost::getPostName, post.getPostName()));
    }
}
