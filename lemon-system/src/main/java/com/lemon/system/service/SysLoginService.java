package com.lemon.system.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lemon.common.constant.CacheConstants;
import com.lemon.common.constant.Constants;
import com.lemon.common.constant.UserConstant;
import com.lemon.common.core.domain.entity.SysUser;
import com.lemon.common.core.domain.event.LoginInfoEvent;
import com.lemon.common.core.domain.model.LoginBody;
import com.lemon.common.core.domain.model.LoginUser;
import com.lemon.common.enums.DeviceType;
import com.lemon.common.enums.LoginType;
import com.lemon.common.exception.user.CaptchaErrorException;
import com.lemon.common.exception.user.CaptchaExpireException;
import com.lemon.common.exception.user.UserException;
import com.lemon.common.helper.LoginHelper;
import com.lemon.common.utils.MessageUtils;
import com.lemon.common.utils.ServletUtils;
import com.lemon.common.utils.StringUtils;
import com.lemon.common.utils.redis.RedisUtils;
import com.lemon.common.utils.spring.SpringUtils;
import com.lemon.framework.config.properties.CaptchaProperties;
import com.lemon.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;

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

    /**
     * 验证码相关配置，详见 application.yml 中 captcha 配置项
     */
    private final CaptchaProperties captchaProperties;

    private final SysUserMapper userMapper;

    /**
     * 登录
     */
    public String login(LoginBody loginBody) {
        //验证码，通过开关确定是否开启校验
        if (captchaProperties.getEnabled()) {
            validateCaptcha(loginBody);
        }

        //根据 username 查询
        SysUser user = loadUserByUserName(loginBody.getUsername());

        checkLogin(LoginType.PASSWORD, loginBody, user);
        //构建 loginUser，用来生成token
        LoginUser loginUser = buildLoginUser(user);
        //生成token
        LoginHelper.loginByDevice(loginUser, DeviceType.PC);

        //记录登录日志
        recordLoginInfo(loginBody.getUsername(), Constants.LOGIN_SUCCESS, MessageUtils.message("user.login.success"));
        //更新用户表登录信息
        updateUserLogin(user.getUserId(), loginBody.getUsername());

        //通过sa-token获取当前对话token值
        return StpUtil.getTokenValue();
    }

    /**
     * 更新用户表登录信息
     * @param userId 用户id
     * @param username 用户名
     */
    private void updateUserLogin(Long userId, String username) {
        SysUser sysUser = new SysUser();
        sysUser.setUserId(userId);
        sysUser.setUpdateBy(username);
        sysUser.setLoginIp(ServletUtils.getClientIP());
        sysUser.setLoginDate(new Date());

        userMapper.updateById(sysUser);
    }

    /**
     * 记录登录日志
     * @param username 用户名
     * @param status 状态
     * @param message 消息内容
     */
    private void recordLoginInfo(String username, String status, String message) {
        LoginInfoEvent loginInfoEvent = new LoginInfoEvent();
        loginInfoEvent.setUsername(username);
        loginInfoEvent.setStatus(status);
        loginInfoEvent.setMessage(message);
        //获取请求
        loginInfoEvent.setRequest(ServletUtils.getRequest());
        //发送事件  会被 SysLoginInfoServiceImpl 中的 recordLoginInfo 方法处理
        SpringUtils.getApplicationContext().publishEvent(loginInfoEvent);
    }

    /**
     * 验证码校验
     */
    private void validateCaptcha(LoginBody loginBody) {
        // redis中存储验证码的 key，命名为 captcha_codes: uuid (生成验证码时的唯一标识)
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + StringUtils.defaultString(loginBody.getUuid(), "");
        // 从redis中获取验证码
        String captcha = RedisUtils.getCacheObject(verifyKey);
        // 验证码使用之后即销毁
        RedisUtils.deleteObject(verifyKey);

        //如果验证码不存在，说明已过期，返回提示
        if (captcha == null) {
            recordLoginInfo(loginBody.getUsername(), Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.expire"));
            throw new CaptchaExpireException();
        }
        // 验证码不一致，返回验证码错误提示，不考虑大小写
        if (!captcha.equalsIgnoreCase(loginBody.getCode())) {
            recordLoginInfo(loginBody.getUsername(), Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.expire"));
            throw new CaptchaErrorException();
        }
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
            recordLoginInfo(loginBody.getUsername(), loginFail, MessageUtils.message(loginType.getRetryLimitExceed(), maxRetryCount, lockTime));
            throw new UserException(loginType.getRetryLimitExceed(), maxRetryCount, lockTime);
        }

        //登陆失败
        if (!user.getPassword().equals(loginBody.getPassword())) {
            // 错误次数 + 1，并在缓存中更新
            errorNumber++;
            RedisUtils.setCacheObject(errorKey, errorNumber, Duration.ofMinutes(lockTime));
            // 达到规定错误次数 则锁定登录
            if (errorNumber >= maxRetryCount) {
                recordLoginInfo(loginBody.getUsername(), loginFail, MessageUtils.message(loginType.getRetryLimitExceed(), maxRetryCount, lockTime));
                throw new UserException(loginType.getRetryLimitExceed(), maxRetryCount, lockTime);
            } else {
                // 未达到规定错误次数
                recordLoginInfo(loginBody.getUsername(), loginFail, MessageUtils.message(loginType.getRetryLimitCount(), maxRetryCount, errorNumber));
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
