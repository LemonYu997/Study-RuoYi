package com.lemon.common.utils.ip;

import cn.hutool.core.net.NetUtil;
import cn.hutool.http.HtmlUtil;
import com.lemon.common.utils.StringUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 获取地址工具类
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AddressUtils {
    //未知地址
    public static final String UNKNOWN = "XX XX";

    /**
     * 根据IP获取真实地址
     */
    public static String getRealAddressByIP(String ip) {
        if (StringUtils.isBlank(ip)) {
            return UNKNOWN;
        }
        // HtmlUtil.cleanHtmlTag(ip) 清除传入 ip 字符串的 HTML 的<>标签
        ip = StringUtils.contains(ip, "0:0:0:0:0:0:0:1") ? "127.0.0.1" : HtmlUtil.cleanHtmlTag(ip);
        // 内网不查询
        if (NetUtil.isInnerIP(ip)) {
            return "内网IP";
        }
        //使用 IP2region 查询实际地址
        return RegionUtils.getCityInfo(ip);
    }
}
