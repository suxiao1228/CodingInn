package com.xiongsu.service.article.service.impl;

import com.xiongsu.service.article.cache.ArticleCacheManager;
import com.xiongsu.service.article.repository.dao.ArticleDao;
import com.xiongsu.service.article.repository.dao.ColumnArticleDao;
import com.xiongsu.service.article.repository.dao.ColumnDao;
import com.xiongsu.service.article.repository.entity.ColumnArticleDO;
import com.xiongsu.service.article.service.ColumnService;
import com.xiongsu.service.user.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;

public class ColumnServiceImpl implements ColumnService {

    @Resource
    private ColumnDao columnDao;
    @Resource
    private ArticleDao articleDao;

    @Resource
    private ColumnArticleDao columnArticleDao;

    @Resource
    private UserService userService;

    @Resource
    private ArticleCacheManager articleCacheManager;

    @Override
    public ColumnArticleDO getColumnArticleRelation(Long articleId) {
        if(articleCacheManager.isArticleColumnArticleExist(articleId)) {
            return articleCacheManager.getColumnArticle(articleId);
        }
        ColumnArticleDO columnArticleDO = columnArticleDao.selectColumnArticleByArticleId(articleId);
        articleCacheManager.setColumnArticle(articleId, columnArticleDO);
        return columnArticleDO;
    }
}
