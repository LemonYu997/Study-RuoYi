package com.lemon.controller.system;

import cn.dev33.satoken.annotation.SaIgnore;
import com.lemon.common.constant.Constants;
import com.lemon.common.core.domain.R;
import com.lemon.common.core.domain.entity.SysUser;
import com.lemon.common.core.domain.model.LoginBody;
import com.lemon.common.core.domain.model.LoginUser;
import com.lemon.common.helper.LoginHelper;
import com.lemon.system.service.ISysUserService;
import com.lemon.system.service.SysLoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 登录验证
 * 注解 @Validated，开启请求参数注解校验
 */
@RequiredArgsConstructor
@RestController
public class SysLoginController {

    private final SysLoginService loginService;
    private final ISysUserService sysUserService;

    /**
     * 登录方法
     */
    @SaIgnore   //过滤Sa-Token登录检查
    @PostMapping("/login")
    public R<Map<String ,Object>> login(@Validated @RequestBody LoginBody loginBody) {
        Map<String ,Object> ajax = new HashMap<>();
        //生成token
        String token = loginService.login(loginBody);
        ajax.put(Constants.TOKEN, token);
        return R.ok(ajax);
    }

    /**
     * 退出登录
     */
    @SaIgnore
    @PostMapping("/logout")
    public R<Void> logout() {
        loginService.logout();
        return R.ok("退出成功");
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/getInfo")
    public R<Map<String, Object>> getInfo() {
        LoginUser loginUser = LoginHelper.getLoginUser();
        SysUser user = sysUserService.selectUserById(loginUser.getUserId());
        Map<String, Object> ajax = new HashMap<>();
        ajax.put("user", user);
        //todo 权限信息
        return R.ok(ajax);
    }
}
