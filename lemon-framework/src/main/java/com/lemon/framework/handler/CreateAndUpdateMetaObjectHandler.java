package com.lemon.framework.handler;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpStatus;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.lemon.common.core.domain.BaseEntity;
import com.lemon.common.core.domain.model.LoginUser;
import com.lemon.common.exception.ServiceException;
import com.lemon.common.helper.LoginHelper;
import com.lemon.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;

import java.util.Date;

/**
 * Mybatis-Plus 字段填充器
 */
@Slf4j
public class CreateAndUpdateMetaObjectHandler implements MetaObjectHandler {
    /**
     * 新增时填充
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        try {
            //只有基础实体类才需要注入这些信息
            if (ObjectUtil.isNotNull(metaObject) && metaObject.getOriginalObject() instanceof BaseEntity) {
                BaseEntity baseEntity = (BaseEntity) metaObject.getOriginalObject();
                //如果实体对象已经设置创建时间和创建者时，按设置的类，否则自动注入
                Date current = ObjectUtil.isNotNull(baseEntity.getCreateTime())
                    ? baseEntity.getCreateTime() : new Date();
                baseEntity.setCreateTime(current);
                baseEntity.setUpdateTime(current);
                String username = StringUtils.isNotBlank(baseEntity.getCreateBy())
                    ? baseEntity.getCreateBy() : getLoginUsername();
                baseEntity.setCreateBy(username);
                baseEntity.setUpdateBy(username);
            }
        } catch (Exception e) {
            throw new ServiceException("自动注入异常 => " + e.getMessage(), HttpStatus.HTTP_UNAUTHORIZED);
        }
    }

    /**
     * 更新时填充
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        try {
            //只有基础实体类才需要注入这些信息
            if (ObjectUtil.isNotNull(metaObject) && metaObject.getOriginalObject() instanceof BaseEntity) {
                BaseEntity baseEntity = (BaseEntity) metaObject.getOriginalObject();
                // 填充更新时间，不管自己设置的
                Date current =  new Date();
                baseEntity.setUpdateTime(current);
                // 存在当前用户时 才填充，不管自己设置的
                String username = getLoginUsername();
                if (StringUtils.isNotBlank(username)) {
                    baseEntity.setUpdateBy(username);
                }
            }
        } catch (Exception e) {
            throw new ServiceException("自动注入异常 => " + e.getMessage(), HttpStatus.HTTP_UNAUTHORIZED);
        }
    }

    /**
     * 获取登录用户名
     */
    private String getLoginUsername() {
        LoginUser loginUser;
        try {
            loginUser = LoginHelper.getLoginUser();
        } catch (Exception e) {
            log.warn("自动注入警告 => 用户未登录");
            return null;
        }
        return ObjectUtil.isNotNull(loginUser) ? loginUser.getUsername() : null;
    }
}
