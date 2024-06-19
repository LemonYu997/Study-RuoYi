package com.lemon.oss.entity;

import lombok.Builder;
import lombok.Data;

/**
 * 文件上传返回体
 */
@Data
@Builder    //通过链式调用建造者模式中的方法进行对象的构建
public class UploadResult {
    /**
     * 文件路径
     */
    private String url;

    /**
     * 文件名
     */
    private String filename;
}
