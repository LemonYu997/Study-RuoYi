package com.lemon.common.exception.user;

/**
 * 验证码错误异常
 */
public class CaptchaErrorException extends  UserException{
    private static final long serialVersionUID = 1L;

    public CaptchaErrorException() {
        super("user.jcaptcha.error");
    }
}
