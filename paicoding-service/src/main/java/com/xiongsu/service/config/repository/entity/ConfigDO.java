package com.xiongsu.service.config.repository.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xiongsu.api.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 评论表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("config")
public class ConfigDO extends BaseDO {
    private static final long serialVersionUID = -6122208316544171303L;//它的作用是在对象进行 序列化（Serialization） 和 反序列化（Deserialization） 时，确保类的兼容性

    /**
     * 类型
     */
    private Integer type;

    /**
     * 名称
     */
    @TableField("'name'")//映射数据库字段
    private String name;

    /**
     * 图片链接
     */
    private String bannerUrl;

    /**
     * 跳转链接
     */
    private String jumpUrl;

    /**
     * 内容
     */
    private String content;

    /**
     * 排序
     */
    @TableField("'rank'")//映射数据库字段
    private Integer rank;

    /**
     * 状态 0-未发布 1-已发布
     */
    private Integer status;

    /**
     * 0未删除 1已删除
     */
    private Integer deleted;

    /**
     * 配置对应的标签，英文逗号分割
     */
    private String tags;

    /**
     * 扩展信息，如 记录 评分，阅读人数，下载次数等
     */
    private String extra;
}
