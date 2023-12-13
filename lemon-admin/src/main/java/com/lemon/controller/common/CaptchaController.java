package com.lemon.controller.common;

import cn.dev33.satoken.annotation.SaIgnore;
import cn.hutool.captcha.AbstractCaptcha;
import cn.hutool.captcha.generator.CodeGenerator;
import cn.hutool.core.util.IdUtil;
import com.lemon.common.constant.CacheConstants;
import com.lemon.common.constant.Constants;
import com.lemon.common.core.domain.R;
import com.lemon.common.enums.CaptchaType;
import com.lemon.common.utils.StringUtils;
import com.lemon.common.utils.redis.RedisUtils;
import com.lemon.common.utils.reflect.ReflectUtils;
import com.lemon.common.utils.spring.SpringUtils;
import com.lemon.framework.config.properties.CaptchaProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 生成验证码
 * 注解 @SaIgnore，接口不需要进行登录校验，不会被sa-token拦截器拦截
 */
@SaIgnore
@Slf4j
@RequiredArgsConstructor
@RestController
public class CaptchaController {
    /**
     * 验证码相关配置，详见 application.yml 中 captcha 配置项
     */
    private final CaptchaProperties captchaProperties;

    /**
     * 生成图片验证码
     */
    @GetMapping("/captchaImage")
    public R<Map<String, Object>> getCode() {
        Map<String, Object> ajax = new HashMap<>();
        //获取验证码是否开启配置
        boolean captchaEnabled = captchaProperties.getEnabled();
        ajax.put("captchaEnabled", captchaEnabled);
        // 如果未开启验证码，直接返回，告诉前端不用生成验证码
        if (!captchaEnabled) {
            return R.ok(ajax);
        }
        //验证码基础信息，用 redis 存储
        String uuid = IdUtil.simpleUUID();
        // redis中存储验证码的 key，命名为 captcha_codes: uuid (生成验证码时的唯一标识)
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + uuid;

        //生成验证码
        //验证码类型 数字 或 字符
        CaptchaType captchaType = captchaProperties.getType();
        boolean isMath = CaptchaType.MATH == captchaType;
        // 如果是数字类型，长度根据配置中数字长度获取，否则使用字符长度
        Integer length = isMath ? captchaProperties.getNumberLength() : captchaProperties.getCharLength();
        CodeGenerator codeGenerator = ReflectUtils.newInstance(captchaType.getClazz(), length);
        AbstractCaptcha captcha = SpringUtils.getBean(captchaProperties.getCategory().getClazz());
        captcha.setGenerator(codeGenerator);
        captcha.createCode();
        String code = captcha.getCode();
        if (isMath) {
            ExpressionParser parser = new SpelExpressionParser();
            Expression exp = parser.parseExpression(StringUtils.remove(code, "="));
            code = exp.getValue(String.class);
        }
        //存储验证码
        RedisUtils.setCacheObject(verifyKey, code, Duration.ofMinutes(Constants.CAPTCHA_EXPIRATION));
        ajax.put("uuid", uuid);
        ajax.put("img", captcha.getImageBase64());
        return R.ok(ajax);
    }
}
