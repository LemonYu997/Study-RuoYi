package com.lemon.framework.exception;

import com.lemon.common.core.domain.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

/**
 * 全局异常处理
 */
@Slf4j
@RestControllerAdvice   //声明全局异常类
public class GlobalExceptionHandler {

    /**
     * 系统异常
     */
    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e, HttpServletRequest request) {
        String uri = request.getRequestURI();
        log.error("请求地址'{}'，发生系统异常", uri, e);
        return R.fail(e.getMessage());
    }

    /**
     * 未知运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public R<Void> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        String uri = request.getRequestURI();
        log.error("请求地址'{}'，发生未知异常", uri, e);
        return R.fail(e.getMessage());
    }
}
