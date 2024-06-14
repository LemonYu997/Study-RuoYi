package com.lemon.system.service;

import cn.dev33.satoken.secure.BCrypt;
import com.lemon.common.constant.CacheConstants;
import com.lemon.common.constant.Constants;
import com.lemon.common.core.domain.entity.SysUser;
import com.lemon.common.core.domain.event.LoginInfoEvent;
import com.lemon.common.core.domain.model.RegisterBody;
import com.lemon.common.enums.UserType;
import com.lemon.common.exception.user.CaptchaErrorException;
import com.lemon.common.exception.user.CaptchaExpireException;
import com.lemon.common.exception.user.UserException;
import com.lemon.common.utils.MessageUtils;
import com.lemon.common.utils.ServletUtils;
import com.lemon.common.utils.StringUtils;
import com.lemon.common.utils.redis.RedisUtils;
import com.lemon.common.utils.spring.SpringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 注册校验方法
 */
@RequiredArgsConstructor
@Service
public class SysRegisterService {

    private final ISysUserService userService;
    private final ISysConfigService configService;

    /**
     * 注册
     */
    public void register(RegisterBody registerBody) {
        String username = registerBody.getUsername();
        String password = registerBody.getPassword();
        // 校验用户类型是否存在
        String userType = UserType.getUserType(registerBody.getUserType()).getUserType();
        // 验证码开关配置
        boolean captchaEnabled = configService.selectCaptchaEnabled();
        // 验证码开关
        if (captchaEnabled) {
            // 校验验证码
            validateCaptcha(username, registerBody.getCode(), registerBody.getUuid());
        }
        SysUser user = new SysUser();
        user.setUserName(username);
        user.setNickName(username);
        user.setPassword(BCrypt.hashpw(password));
        user.setUserType(userType);

        if (!userService.checkUserNameUnique(user)) {
            throw new UserException("user.register.save.error", username);
        }
        // 向数据库中填充
        boolean regFlag = userService.registerUser(user);
        if (!regFlag) {
            throw new UserException("user.register.error");
        }
        recordLoginInfo(username, Constants.REGISTER, MessageUtils.message("user.register.success"));
    }

    /**
     * 校验验证码
     *
     * @param username 用户名
     * @param code     验证码
     * @param uuid     唯一标识
     */
    private void validateCaptcha(String username, String code, String uuid) {
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + StringUtils.defaultString(uuid, "");
        String captcha = RedisUtils.getCacheObject(verifyKey);
        RedisUtils.deleteObject(verifyKey);
        // 验证码过期
        if (captcha == null) {
            // 记录登录信息
            recordLoginInfo(username, Constants.REGISTER, MessageUtils.message("user.jcaptcha.expire"));
            throw new CaptchaExpireException();
        }
        // 验证码错误
        if (!code.equalsIgnoreCase(captcha)) {
            recordLoginInfo(username, Constants.REGISTER,  MessageUtils.message("user.jcaptcha.error"));
            throw new CaptchaErrorException();
        }
    }

    /**
     * 记录登录信息
     *
     * @param username 用户名
     * @param status   状态
     * @param message  消息内容
     * @return
     */
    private void recordLoginInfo(String username, String status, String message) {
        LoginInfoEvent loginInfoEvent = new LoginInfoEvent();
        loginInfoEvent.setUsername(username);
        loginInfoEvent.setStatus(status);
        loginInfoEvent.setMessage(message);
        loginInfoEvent.setRequest(ServletUtils.getRequest());
        SpringUtils.context().publishEvent(loginInfoEvent);
    }
}
