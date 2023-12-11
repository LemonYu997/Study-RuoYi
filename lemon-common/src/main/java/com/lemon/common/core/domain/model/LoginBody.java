package com.lemon.common.core.domain.model;

import com.lemon.common.constant.UserConstant;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

/**
 * 用户登录对象
 */
@Data
public class LoginBody {
    /**
     * 用户名
     */
    @NotBlank
    @Length(min = UserConstant.USERNAME_MIN_LENGTH, max = UserConstant.USERNAME_MAX_LENGTH)
    private String username;

    /**
     * 密码
     */
    @NotBlank
    @Length(min = UserConstant.PASSWORD_MIN_LENGTH, max = UserConstant.PASSWORD_MAX_LENGTH)
    private String password;

    /**
     * 验证码
     */
    private String code;

    /**
     * 唯一标识
     */
    private String uuid;
}
