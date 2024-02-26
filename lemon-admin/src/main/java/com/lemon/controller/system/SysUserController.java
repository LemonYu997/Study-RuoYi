package com.lemon.controller.system;

import cn.dev33.satoken.secure.BCrypt;
import com.lemon.common.annotation.Log;
import com.lemon.common.core.controller.BaseController;
import com.lemon.common.core.domain.R;
import com.lemon.common.core.domain.entity.SysUser;
import com.lemon.common.core.page.PageQuery;
import com.lemon.common.core.page.TableDataInfo;
import com.lemon.common.enums.BusinessType;
import com.lemon.common.utils.StringUtils;
import com.lemon.system.service.ISysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户信息管理
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/user")
public class SysUserController extends BaseController {

    private final ISysUserService userService;

    /**
     * 分页获取用户列表
     */
    @GetMapping("/list")
    public TableDataInfo<SysUser> list(SysUser user, PageQuery pageQuery) {
        return userService.selectPageUserList(user, pageQuery);
    }


    /**
     * 根据用户编号获取详细信息
     *
     * @param userId 用户ID
     */
    @GetMapping(value = "/{userId}")
    public R<SysUser> getInfo(@PathVariable(value = "userId") Long userId) {
        return R.ok(userService.selectUserById(userId));
    }

    /**
     * 新增用户
     */
    @PostMapping
    @Log(title = "用户管理", businessType = BusinessType.INSERT)
    public R<Void> add(@RequestBody SysUser user) {
        //重复账号校验、重复手机号校验、重复邮箱校验
        if (!userService.checkUserNameUnique(user)) {
            return R.fail("新增用户'" + user.getUserName() + "'失败，登录账号已存在");
        } else if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(user)) {
            return R.fail("新增用户'" + user.getUserName() + "'失败，手机号码已存在");
        } else if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user)) {
            return R.fail("新增用户'" + user.getUserName() + "'失败，邮箱已存在");
        }
        //密码加密
        user.setPassword(BCrypt.hashpw(user.getPassword()));
        //入库
        return toAjax(userService.insertUser(user));
    }

    /**
     * 修改用户
     */
    @PutMapping
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    public R<Void> edit(@Validated @RequestBody SysUser user) {
        if (!userService.checkUserNameUnique(user)) {
            return R.fail("修改用户'" + user.getUserName() + "'失败，登录账号已存在");
        } else if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(user)) {
            return R.fail("修改用户'" + user.getUserName() + "'失败，手机号码已存在");
        } else if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user)) {
            return R.fail("修改用户'" + user.getUserName() + "'失败，邮箱账号已存在");
        }
        // todo 这里不能修改密码
        return toAjax(userService.updateUser(user));
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{userIds}")
    @Log(title = "用户管理", businessType = BusinessType.DELETE)
    public R<Void> remove(@PathVariable Long[] userIds) {
        return toAjax(userService.deleteUserByIds(userIds));
    }
}
