package com.lemon.common.core.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 基础 Entity
 */
@Data
public class BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 搜索值
     */
    @JsonIgnore     //忽略序列化和反序列化
    @TableField(exist = false)  //表中没有该字段
    private String searchValue;

    /**
     * 创建者
     */
    @TableField(fill = FieldFill.INSERT)    //新增时更新
    private String createBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)    //新增时更新
    private Date createTime;

    /**
     * 更新者
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)    //新增和修改时时更新
    private String updateBy;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)    //新增和修改时时更新
    private Date updateTime;

    /**
     * 请求参数
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY) //排除空值属性
    @TableField(exist = false)  //表中没有该字段
    private Map<String, Object> params = new HashMap<>();
}
