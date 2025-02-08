package com.xiongsu.service.article.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xiongsu.api.entity.BaseDO;
import com.xiongsu.api.enums.column.ColumnArticleReadEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 专栏文章
 *
 * @author YiHui
 * @date 2022/9/14
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("column_article")
public class ColumnArticleDO extends BaseDO {
    private static final long serialVersionUID = -2372103913090667453L; //序列化

    private Long columnId;

    private Long articleId;

    /**
     * 顺序，越小越靠前
     */
    private Integer section;

    /**
     * 专栏类型：免费、登录阅读、收费阅读等
     *
     * @see ColumnArticleReadEnum#getRead()
     */
    private Integer readType;
}
