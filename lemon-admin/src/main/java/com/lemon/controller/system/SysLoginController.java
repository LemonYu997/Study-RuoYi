package com.lemon.controller.system;

import com.lemon.common.constant.Constants;
import com.lemon.common.core.domain.R;
import com.lemon.common.core.domain.model.LoginBody;
import com.lemon.system.service.SysLoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 登录验证
 */
@RequiredArgsConstructor
@RestController
public class SysLoginController {

    private final SysLoginService loginService;

    /**
     * 登录方法
     */
    @PostMapping("/login")
    public R<Map<String ,Object>> login(@RequestBody LoginBody loginBody) {
        Map<String ,Object> ajax = new HashMap<>();
        //生成token
        String token = loginService.login(loginBody);
        ajax.put(Constants.TOKEN, token);
        return R.ok(ajax);
    }
}
