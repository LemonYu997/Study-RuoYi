package com.lemon.common.utils;

import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.lemon.common.utils.spring.SpringUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;

/**
 * Json 工具类
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonUtils {
    private static final ObjectMapper OBJECT_MAPPER = SpringUtils.getBean(ObjectMapper.class);
    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    /**
     * Object -> JsonString
     */
    public static String toJsonString(Object object) {
        if (ObjectUtil.isNull(object)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * str -> dict
     */
    public static Dict parseMap(String str) {
        if (StringUtils.isBlank(str)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(str, OBJECT_MAPPER.getTypeFactory().constructType(Dict.class));
        } catch (MismatchedInputException e) {
            // 类型不匹配说明不是 json
            return null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T parseObject(String text, Class<T> clazz) {
        if (StringUtils.isEmpty(text)) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(text, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
