package com.lemon.system.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lemon.common.constant.UserConstant;
import com.lemon.common.core.domain.entity.SysUser;
import com.lemon.common.core.domain.model.LoginBody;
import com.lemon.common.core.domain.model.LoginUser;
import com.lemon.common.enums.DeviceType;
import com.lemon.common.enums.LoginType;
import com.lemon.common.exception.user.UserException;
import com.lemon.common.helper.LoginHelper;
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

        checkLogin(LoginType.PASSWORD, loginBody, user);
        //构建 loginUser，用来生成token
        LoginUser loginUser = buildLoginUser(user);
        //生成token
        LoginHelper.loginByDevice(loginUser, DeviceType.PC);

        //通过sa-token获取当前对话token值
        return StpUtil.getTokenValue();
    }

    /**
     * 登录校验
     */
    private void checkLogin(LoginType loginType, LoginBody loginBody, SysUser user) {
        if (!user.getPassword().equals(loginBody.getPassword())) {
            throw new UserException("user.password.not.match", loginBody.getUsername());
        }
    }

    /**
     * 构建登录用户
     */
    private LoginUser buildLoginUser(SysUser user) {
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getUserId());
        loginUser.setDeptId(user.getDeptId());
        loginUser.setUsername(user.getUserName());
        loginUser.setUserType(user.getUserType());

        return loginUser;
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
            throw new UserException("user.not.exists", username);
        } else if (UserConstant.EXCEPTION.equals(sysUser.getStatus())) {
            log.info("登录用户：{} 已被停用", username);
            throw new UserException("user.blocked", username);
        }

        //返回完整的用户信息
        return userMapper.selectUserByUsername(username);
    }
}
