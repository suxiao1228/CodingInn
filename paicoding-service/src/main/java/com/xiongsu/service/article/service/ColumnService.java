package com.xiongsu.service.article.service;

import com.xiongsu.service.article.repository.entity.ColumnArticleDO;

public interface ColumnService {


    /**
     * 根据文章id,构建对应的专栏详情地址
     * @param articleId
     * @return
     */
    ColumnArticleDO getColumnArticleRelation(Long articleId);
}
