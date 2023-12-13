package com.lemon.system.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lemon.common.constant.CacheConstants;
import com.lemon.common.constant.Constants;
import com.lemon.common.constant.UserConstant;
import com.lemon.common.core.domain.entity.SysUser;
import com.lemon.common.core.domain.model.LoginBody;
import com.lemon.common.core.domain.model.LoginUser;
import com.lemon.common.enums.DeviceType;
import com.lemon.common.enums.LoginType;
import com.lemon.common.exception.user.UserException;
import com.lemon.common.helper.LoginHelper;
import com.lemon.common.utils.redis.RedisUtils;
import com.lemon.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 登录校验方法
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class SysLoginService {

    /**
     * 登录最大重试次数，见 application.yml 中相关配置项
     */
    @Value("${user.password.maxRetryCount}")
    private Integer maxRetryCount;

    /**
     * 锁定时间，见 application.yml 中相关配置项
     */
    @Value("${user.password.lockTime}")
    private Integer lockTime;

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
     * 记录登录错误次数
     */
    private void checkLogin(LoginType loginType, LoginBody loginBody, SysUser user) {
        String username = loginBody.getUsername();
        //redis 的 key ， 由前缀名 pwd_err_cnt: username 组成
        String errorKey = CacheConstants.PWD_ERR_CNT_KEY + username;
        String loginFail = Constants.LOGIN_FAIL;    //Error

        // 获取用户登录错误的次数，默认为 0
        int errorNumber = ObjectUtil.defaultIfNull(RedisUtils.getCacheObject(errorKey), 0);
        // 超出最大登录次数，则拒绝登录，此时即使密码正确也无法登录
        if (errorNumber >= maxRetryCount) {
            throw new UserException(loginType.getRetryLimitExceed(), maxRetryCount, lockTime);
        }

        //登陆失败
        if (!user.getPassword().equals(loginBody.getPassword())) {
            // 错误次数 + 1，并在缓存中更新
            errorNumber++;
            RedisUtils.setCacheObject(errorKey, errorNumber, Duration.ofMinutes(lockTime));
            // 达到规定错误次数 则锁定登录
            if (errorNumber >= maxRetryCount) {
                throw new UserException(loginType.getRetryLimitExceed(), maxRetryCount, lockTime);
            } else {
                // 未达到规定错误次数
                throw new UserException(loginType.getRetryLimitCount(), errorNumber);
            }
        }

        // 登录成功，清空错误次数
        RedisUtils.deleteObject(errorKey);
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
