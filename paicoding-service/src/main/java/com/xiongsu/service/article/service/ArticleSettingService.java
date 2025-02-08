package com.xiongsu.service.article.service;

import com.xiongsu.api.enums.OperateArticleEnum;
import com.xiongsu.api.vo.PageVo;
import com.xiongsu.api.vo.article.ArticlePostReq;
import com.xiongsu.api.vo.article.SearchArticleReq;
import com.xiongsu.api.vo.article.dto.ArticleAdminDTO;

public interface ArticleSettingService {
    /**
     * 更新文章
     *
     * @param req
     */
    void updateArticle(ArticlePostReq req);

    /**
     * 获取文章列表
     *
     * @param req
     * @return
     */
    PageVo<ArticleAdminDTO> getArticleList(SearchArticleReq req);

    /**
     * 删除文章
     *
     * @param articleId
     */
    void deleteArticle(Long articleId);

    /**
     * 操作文章
     *
     * @param articleId
     * @param operate
     */
    void operateArticle(Long articleId, OperateArticleEnum operate);




}
