package com.lemon.system.service;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lemon.common.constant.UserConstant;
import com.lemon.common.core.domain.entity.SysUser;
import com.lemon.common.core.domain.model.LoginBody;
import com.lemon.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 登录校验方法
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class SysLoginService {

    private final SysUserMapper userMapper;

    /**
     * 登录
     */
    public String login(LoginBody loginBody) {
        //TODO 验证码

        //根据 username 查询
        SysUser user = loadUserByUserName(loginBody.getUsername());

        if (user.getPassword().equals(loginBody.getPassword())) {
            //模拟token
            return "123456";
        }

        return null;
    }

    /**
     * 根据 username 查询
     */
    private SysUser loadUserByUserName(String username) {
        //先判断user是否存在，状态是否正常
        SysUser sysUser = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
            .select(SysUser::getUserName, SysUser::getStatus)
            .eq(SysUser::getUserName, username));

        if (ObjectUtil.isNull(sysUser)) {
            log.info("登录用户：{} 不存在", username);
            return null;
        } else if (UserConstant.EXCEPTION.equals(sysUser.getStatus())) {
            log.info("登录用户：{} 已被停用", username);
            return null;
        }

        //返回完整的用户信息
        return userMapper.selectUserByUsername(username);
    }
}
