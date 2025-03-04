package com.xiongsu.service.article.service.impl;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xiongsu.api.enums.HomeSelectEnum;
import com.xiongsu.api.vo.PageListVo;
import com.xiongsu.api.vo.PageParam;
import com.xiongsu.api.vo.PageVo;
import com.xiongsu.api.vo.article.dto.ArticleDTO;
import com.xiongsu.api.vo.article.dto.SimpleArticleDTO;
import com.xiongsu.api.vo.article.dto.TagDTO;
import com.xiongsu.service.article.repository.entity.ArticleDO;
import com.xiongsu.service.article.service.ArticleReadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 文章查询相关服务类
 */
@Service
@Slf4j
public class ArticleReadServiceImpl implements ArticleReadService {


    @Override
    public ArticleDO queryBasicArticle(Long articleId) {
        return null;
    }

    @Override
    public String generateSummary(String content) {
        return "";
    }

    @Override
    public PageVo<TagDTO> queryTagsByArticleId(Long articleId) {
        return null;
    }

    @Override
    public ArticleDTO queryDetailArticleInfo(Long articleId) {
        return null;
    }

    @Override
    public ArticleDTO queryFullArticleInfo(Long articleId, Long currentUser) {
        return null;
    }

    @Override
    public PageListVo<ArticleDTO> queryArticlesByCategory(Long categoryId, PageParam page) {
        return null;
    }

    @Override
    public IPage<ArticleDTO> queryArticlesByCategoryPagination(int currentPage, int pageSize, String category) {
        return null;
    }

    @Override
    public IPage<ArticleDTO> queryArticlesByTagPagination(int currentPage, int pageSize, Long tagId) {
        return null;
    }

    @Override
    public List<ArticleDTO> queryTopArticlesByCategory(Long categoryId) {
        return List.of();
    }

    @Override
    public Long queryArticleCountByCategory(Long categoryId) {
        return 0;
    }

    @Override
    public Map<Long, Long> queryArticleCountsByCategory() {
        return Map.of();
    }

    @Override
    public PageListVo<ArticleDTO> queryArticlesByTag(Long tagId, PageParam page) {
        return null;
    }

    @Override
    public List<SimpleArticleDTO> querySimpleArticleBySearchKey(String key) {
        return List.of();
    }

    @Override
    public PageListVo<ArticleDTO> queryArticlesBySearchKey(String key, PageParam page) {
        return null;
    }

    @Override
    public PageListVo<ArticleDTO> queryArticlesByUserAndType(Long userId, PageParam pageParam, HomeSelectEnum select) {
        return null;
    }

    @Override
    public IPage<ArticleDTO> queryHistoryArticlesByUserIdPagination(Long userId, int currentPage, int pageSize) {
        return null;
    }

    @Override
    public IPage<ArticleDTO> queryStarArticlesByUserIdPagination(Long userId, int currentPage, int pageSize) {
        return null;
    }

    /**
     * 根据用户id分页查询用户发表的文章
     * @param userId
     * @param currentPage
     * @param pageSize
     * @return
     */
    @Override
    public IPage<ArticleDTO> queryArticlesByUserIdPagination(Long userId, int currentPage, int pageSize) {
        return null;
    }

    @Override
    public PageListVo<ArticleDTO> buildArticleListVo(List<ArticleDO> records, long pageSize) {
        return null;
    }

    @Override
    public PageListVo<SimpleArticleDTO> queryHotArticlesForRecommend(PageParam pageParam) {
        return null;
    }

    @Override
    public int queryArticleCount(long authorId) {
        return 0;
    }

    @Override
    public Long getArticleCount() {
        return 0L;
    }
}
