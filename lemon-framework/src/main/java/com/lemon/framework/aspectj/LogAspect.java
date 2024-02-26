package com.lemon.framework.aspectj;

import cn.hutool.core.lang.Dict;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import com.lemon.common.annotation.Log;
import com.lemon.common.core.domain.event.OperLogEvent;
import com.lemon.common.core.domain.model.LoginUser;
import com.lemon.common.enums.BusinessStatus;
import com.lemon.common.helper.LoginHelper;
import com.lemon.common.utils.JsonUtils;
import com.lemon.common.utils.ServletUtils;
import com.lemon.common.utils.StringUtils;
import com.lemon.common.utils.spring.SpringUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.StringJoiner;

/**
 * 操作日志记录处理
 */
@Slf4j
@Aspect
@Component
public class LogAspect {
    /**
     * 排除敏感属性字段
     */
    public static final String[] EXCLUDE_PROPERTIES = { "password", "oldPassword", "newPassword", "confirmPassword" };

    /**
     * 处理完请求后执行
     * @param joinPoint 切点
     */
    @AfterReturning(pointcut = "@annotation(controllerLog)", returning = "jsonResult")
    public void doAfterReturing(JoinPoint joinPoint, Log controllerLog, Object jsonResult) {
        handleLog(joinPoint, controllerLog, null, jsonResult);
    }

    /**
     * 拦截异常操作
     */
    @AfterThrowing(value = "@annotation(controllerLog)", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Log controllerLog, Exception e) {
        handleLog(joinPoint, controllerLog, e, null);
    }

    /**
     * 处理日志
     */
    protected void handleLog(final JoinPoint joinPoint, Log controllerLog, final Exception e, Object jsonResult) {
        try {
            // 生成日志事件
            OperLogEvent logEvent = new OperLogEvent();
            logEvent.setStatus(BusinessStatus.SUCCESS.ordinal());
            // 请求地址
            String ip = ServletUtils.getClientIP();
            logEvent.setOperIp(ip);
            logEvent.setOperUrl(StringUtils.substring(ServletUtils.getRequest().getRequestURI(), 0, 255));
            LoginUser loginUser = LoginHelper.getLoginUser();
            // todo 如果没有登录这里会报错
            logEvent.setOperName(loginUser.getUsername());
            logEvent.setDeptName(loginUser.getDeptName());

            if (e != null) {
                logEvent.setStatus(BusinessStatus.FAIL.ordinal());
                logEvent.setErrorMsg(StringUtils.substring(e.getMessage(), 0, 2000));
            }
            // 设置方法名称
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = joinPoint.getSignature().getName();
            logEvent.setMethod(className + "." + methodName + "()");
            // 设置请求方式
            logEvent.setRequestMethod(ServletUtils.getRequest().getMethod());
            // 处理设置注解上的参数
            getControllerMethodDescription(joinPoint, controllerLog, logEvent, jsonResult);
            // 发布事件保存数据库
            SpringUtils.context().publishEvent(logEvent);
        } catch (Exception exp) {
            // 记录本地异常日志
            log.error("异常信息：{}", e.getMessage());
            exp.printStackTrace();
        }
    }

    /**
     * 获取注解中对方法的描述信息 用于Controller层注解
     * @param log 日志
     * @param logEvent 操作日志
     */
    private void getControllerMethodDescription(JoinPoint joinPoint, Log log, OperLogEvent logEvent, Object jsonResult) {
        // 设置 action 动作 ordinal 返回该枚举量的位置索引（从0开始）
        logEvent.setBusinessType(log.businessType().ordinal());
        // 设置标题
        logEvent.setTitle(log.title());
        // 设置操作人类别
        logEvent.setOperatorType(log.operatorType().ordinal());
        // 是否需要保存 request 参数和值
        if (log.isSaveRequestData()) {
            // 获取参数的信息，传入到数据库中
            setRequestValue(joinPoint, logEvent, log.excludeParamNames());
        }
        // 是否需要保存 response，参数和值
        if (log.isSaveResponseData()) {
            logEvent.setJsonResult(StringUtils.substring(JsonUtils.toJsonString(jsonResult), 0, 2000));
        }
    }

    /**
     * 获取请求的参数，放入 log 中
     * @param logEvent 操作日志
     */
    private void setRequestValue(JoinPoint joinPoint, OperLogEvent logEvent, String[] excludeParamNames) {
        Map<String, String> paramMap = ServletUtils.getParamMap(ServletUtils.getRequest());
        String requestMethod = logEvent.getRequestMethod();
        // 如果是 PUT 和 POST 请求，且没有 param 传参，那应该是 body 传参
        if (MapUtil.isEmpty(paramMap)
            && HttpMethod.PUT.name().equals(requestMethod)
            || HttpMethod.POST.name().equals(requestMethod)) {
            String params = argsArrayToString(joinPoint.getArgs(), excludeParamNames);
            logEvent.setOperParam(StringUtils.substring(params, 0, 2000));
        } else {
            // 数据脱敏
            MapUtil.removeAny(paramMap, EXCLUDE_PROPERTIES);
            MapUtil.removeAny(paramMap, excludeParamNames);
            logEvent.setOperParam(StringUtils.substring(JsonUtils.toJsonString(paramMap), 0, 2000));
        }
    }

    /**
     * 参数拼接
     */
    private String argsArrayToString(Object[] paramsArray, String[] excludeParamNames) {
        StringJoiner params = new StringJoiner(" ");
        if (ArrayUtil.isEmpty(paramsArray)) {
            return params.toString();
        }
        for (Object o : paramsArray) {
            // 判断对象是否需要过滤
            if (ObjectUtil.isNull(o) && !isFilterObject(o)) {
                String str = JsonUtils.toJsonString(o);
                Dict dict = JsonUtils.parseMap(str);
                if (MapUtil.isNotEmpty(dict)) {
                    MapUtil.removeAny(dict, EXCLUDE_PROPERTIES);
                    MapUtil.removeAny(dict, excludeParamNames);
                    str = JsonUtils.toJsonString(dict);
                }
                params.add(str);
            }
        }
        return params.toString();
    }

    /**
     * 判断是否需要过滤的对象
     * @param o 对象信息
     * @return 如果需要过滤，返回true，否则返回 false
     */
    @SuppressWarnings("rawtypes")   // 屏蔽编译器警告，rawtypes 表示不用提示基本类型相关
    private boolean isFilterObject(Object o) {
        Class<?> clazz = o.getClass();
        if (clazz.isArray()){
            // 过滤文件类型
            return clazz.getComponentType().isAssignableFrom(MultipartFile.class);
        } else if (Collection.class.isAssignableFrom(clazz)) {
            Collection collection = (Collection) o;
            for (Object value : collection) {
                // 过滤文件类型
                return value instanceof MultipartFile;
            }
        } else if (Map.class.isAssignableFrom(clazz)) {
            Map map = (Map) o;
            for (Object value : map.values()) {
                return value instanceof MultipartFile;
            }
        }
        return o instanceof MultipartFile
            || o instanceof HttpServletRequest
            || o instanceof HttpServletResponse
            || o instanceof BindingResult;
    }
}
