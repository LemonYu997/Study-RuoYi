package com.lemon.framework.handler;

import com.lemon.common.utils.StringUtils;
import org.redisson.api.NameMapper;

/**
 * redisson 缓存的 key 前缀处理
 */
public class KeyPrefixHandler implements NameMapper {
    private final String keyPrefix;

    public KeyPrefixHandler(String keyPrefix) {
        //前缀为空 则返回空前缀
        this.keyPrefix = StringUtils.isBlank(keyPrefix) ? "" : keyPrefix + ":";
    }

    /**
     * 增加前缀
     */
    @Override
    public String map(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        //如果设置了前缀，并且当前 key 不以前缀开头，就加上前缀
        if (StringUtils.isNotBlank(keyPrefix) && !name.startsWith(keyPrefix)) {
            return keyPrefix + name;
        }
        return name;
    }

    /**
     * 去除前缀
     */
    @Override
    public String unmap(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        //如果设置了前缀，并且当前 key 以该前缀开头，就去除该前缀
        if (StringUtils.isNotBlank(keyPrefix) && name.startsWith(keyPrefix)) {
            return name.substring(keyPrefix.length());
        }
        return name;
    }
}
