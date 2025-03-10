package com.xiongsu.service.article.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiongsu.api.vo.article.dto.ArticleDTO;
import com.xiongsu.core.cache.RedisClient;
import com.xiongsu.core.config.ArticleCacheProperties;
import com.xiongsu.service.article.repository.entity.ColumnArticleDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 管理文章相关信息的缓存
 */

@Component
//将外部配置绑定到 Java 类的核心注解，
// 通常用于将 application.yml 或 application.properties 中的配置值注入到代码中。
@EnableConfigurationProperties(ArticleCacheProperties.class)
public class ArticleCacheManager {

    @Autowired
    private ArticleCacheProperties articleCacheProperties;

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static final String ARTICLE_SCORE_PREFIX = "article_score:";

    public static final String ARTICLE_INFO_PREFIX = "article_info:";

    public static final String ARTICLE_CONTENT_PREFIX = "article_content:";

    public static final String ARTICLE_COLUMN_RELATION_PREFIX = "article_column_relation:";

    public static final String ARTICLE_COMMENTS_PREFIX = "article_comments:";

    public static final String ARTICLE_HOT_COMMENT_PREFIX = "article_hot_comment:";


    public boolean isArticleColumnArticleExist(long articleId){
        return RedisClient.exists(ARTICLE_COLUMN_RELATION_PREFIX + articleId);
    }

    public void setColumnArticle(long articleId, ColumnArticleDO columnArticleDO) {
        RedisClient.setObject(ARTICLE_COLUMN_RELATION_PREFIX + articleId, columnArticleDO);
    }

    public ColumnArticleDO getColumnArticle(long articleId) {
        Object value = RedisClient.getObject(ARTICLE_COLUMN_RELATION_PREFIX + articleId);
        if(value != null) {
            return OBJECT_MAPPER.convertValue(value, ColumnArticleDO.class);
        }
        return null;
    }

    public ArticleDTO getArticleInfo(long articleId) {
        Object value = RedisClient.getObject(ARTICLE_INFO_PREFIX + articleId);
        if (value != null){
            return OBJECT_MAPPER.convertValue(value, ArticleDTO.class);
        }
        return null;
    }

    public void setArticleInfo (long articleId, ArticleDTO articleDTO) {
        RedisClient.setObject(ARTICLE_INFO_PREFIX + articleId, articleDTO);
    }
}
