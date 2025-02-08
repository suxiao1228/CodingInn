package com.xiongsu.service.article.service.impl;

import com.xiongsu.api.vo.PageListVo;
import com.xiongsu.api.vo.PageParam;
import com.xiongsu.api.vo.article.dto.ArticleDTO;
import com.xiongsu.service.article.repository.dao.ArticleDao;
import com.xiongsu.service.article.repository.dao.ArticleTagDao;
import com.xiongsu.service.article.repository.entity.ArticleDO;
import com.xiongsu.service.article.repository.entity.ArticleTagDO;
import com.xiongsu.service.article.service.ArticleReadService;
import com.xiongsu.service.article.service.ArticleRecommendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ArticleRecommendServiceImpl implements ArticleRecommendService {

    @Autowired
    private ArticleDao articleDao;
    @Autowired
    private ArticleTagDao articleTagDao;
    @Autowired
    private ArticleReadService articleReadService;
    @Autowired
    private SidebarService sidebarService;

    @Override
    public PageListVo<ArticleDTO> relatedRecommend(Long articleId, PageParam pageParam) {
        ArticleDO article = articleDao.getById(articleId);
        if(article == null) {
            return PageListVo.emptyVo();
        }
        List<Long> tagIds = articleTagDao.listArticleTags(articleId).stream()
                .map(ArticleTagDO::getTagId).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(tagIds)) {
            return PageListVo.emptyVo();
        }

        List<ArticleDO> recommendArticles = articleDao.listRelatedArticlesOrderByReadCount(article.getCategoryId(), tagIds, pageParam);
        if (recommendArticles.removeIf(s -> s.getId().equals(articleId))) {
            // 移除推荐列表中的当前文章
            pageParam.setPageSize(pageParam.getPageSize() - 1);
        }
        return articleReadService.buildArticleListVo(recommendArticles, pageParam.getPageSize());
    }
}
