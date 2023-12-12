package com.lemon.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 登录设备类型
 */
@Getter
@AllArgsConstructor
public enum DeviceType {
    /**
     * PC端
     */
    PC("pc"),

    /**
     * APP端
     */
    APP("app"),

    /**
     * 小程序端
     */
    XCX("xcx");

    private final String device;

}
