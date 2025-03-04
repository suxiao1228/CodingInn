package com.xiongsu.service.user.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xiongsu.api.entity.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.javassist.SerialVersionUID;

/**
 * 用户关系表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_relation")
public class UserRelationDO extends BaseDO {

    private static final long serialVersionUID = 1L;

    /**
     * 主用户ID
     */
    private Long userId;

    /**
     * 粉丝用户ID
     */
    private Long followUserId;

    /**
     * 关注状态 : 0-未关注 1-已关注  2-取消关注
     */
    private Long followState;
}
