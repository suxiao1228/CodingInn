package com.xiongsu.service.article.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xiongsu.api.vo.article.dto.ColumnDTO;
import com.xiongsu.api.vo.article.dto.SimpleArticleDTO;
import com.xiongsu.service.article.repository.entity.ColumnArticleDO;

import java.util.List;

public interface ColumnService {


    /**
     * 根据文章id,构建对应的专栏详情地址
     * @param articleId
     * @return
     */
    ColumnArticleDO getColumnArticleRelation(Long articleId);

    /**
     * 使用mybatis-plus的分页
     * 专栏列表
     * @param currentPage
     * @param pageSize
     * @return
     */
    IPage<ColumnDTO> listColumnByPage(Long currentPage, Long pageSize);

    /**
     * 只查询基本的专栏信息，不需要统计、作者等信息
     *
     * @param columnId
     * @return
     */
    ColumnDTO queryBasicColumnInfo(Long columnId);

    /**
     * 获取专栏中的第N篇文章
     *
     * @param columnId
     * @param order
     * @return
     */
    ColumnArticleDO queryColumnArticle(long columnId, Integer order);

    /**
     * 专栏 + 文章列表详情
     *
     * @param columnId
     * @return
     */
    List<SimpleArticleDTO> queryColumnArticles(long columnId);
}
