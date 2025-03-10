package com.xiongsu.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class BaseDO implements Serializable {

    // 表示主键值由数据库自动生成（如 MySQL 的 AUTO_INCREMENT）。
    //插入数据时，该字段的值会被忽略，由数据库自动填充。
    @TableId(type = IdType.AUTO)
    private Long id;

    private Date createTime;

    private Date updateTime;
}
