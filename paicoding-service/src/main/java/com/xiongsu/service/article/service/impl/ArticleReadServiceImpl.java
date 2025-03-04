package com.xiongsu.service.article.service.impl;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiongsu.api.enums.HomeSelectEnum;
import com.xiongsu.api.vo.PageListVo;
import com.xiongsu.api.vo.PageParam;
import com.xiongsu.api.vo.PageVo;
import com.xiongsu.api.vo.article.dto.ArticleDTO;
import com.xiongsu.api.vo.article.dto.SimpleArticleDTO;
import com.xiongsu.api.vo.article.dto.TagDTO;
import com.xiongsu.api.vo.user.dto.BaseUserInfoDTO;
import com.xiongsu.service.article.conveter.ArticleConverter;
import com.xiongsu.service.article.repository.dao.ArticleDao;
import com.xiongsu.service.article.repository.dao.ArticleTagDao;
import com.xiongsu.service.article.repository.entity.ArticleDO;
import com.xiongsu.service.article.service.ArticleReadService;
import com.xiongsu.service.article.service.CategoryService;
import com.xiongsu.service.statistics.service.CountService;
import com.xiongsu.service.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 文章查询相关服务类
 */
@Service
@Slf4j
public class ArticleReadServiceImpl implements ArticleReadService {
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ArticleTagDao articleTagDao;

    @Autowired
    private CountService countService;

    @Autowired
    private UserService userService;

    @Autowired
    private ArticleDao articleDao;

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
        return 0L;
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

    /**
     * 根据用户id分页查询用户浏览的历史文章
     * @param userId
     * @param currentPage
     * @param pageSize
     * @return
     */
    @Override
    public IPage<ArticleDTO> queryHistoryArticlesByUserIdPagination(Long userId, int currentPage, int pageSize) {
        Page<ArticleDO> page = new Page<>(currentPage, pageSize);
        IPage<ArticleDO> articleDOIPage = articleDao.listHistoryArticlesByUserIdPagination(page, userId);

        return articleDOIPage.convert(this::fillArticleRelatedInfo);
    }

    /**
     * 根据用户id分页查询用户收藏的文章
     * @param userId
     * @param currentPage
     * @param pageSize
     * @return
     */
    @Override
    public IPage<ArticleDTO> queryStarArticlesByUserIdPagination(Long userId, int currentPage, int pageSize) {
        Page<ArticleDO> page = new Page<>();
        IPage<ArticleDO> articleDOIPage = articleDao.listStarArticlesByUserIdPagination(page, userId);

        return articleDOIPage.convert(this::fillArticleRelatedInfo);
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

        IPage<ArticleDO> articleDOIPage = articleDao.listArticlesByUserIdPagination(userId, currentPage, pageSize);

        return articleDOIPage.convert(this::fillArticleRelatedInfo);
    }

    /**
     * 补全文章的阅读计数、作者、分类、标签等信息
     *
     * @param record
     * @return
     */
    private ArticleDTO fillArticleRelatedInfo(ArticleDO record) {
        ArticleDTO dto = ArticleConverter.toDto(record);
        // 分类信息
        dto.getCategory().setCategory(categoryService.queryCategoryName(record.getCategoryId()));
        // 标签列表
        dto.setTags(articleTagDao.queryArticleTagDetails(record.getId()));
        // 阅读计数统计
        dto.setCount(countService.queryArticleStatisticInfo(record.getId()));
        // 作者信息
        BaseUserInfoDTO author = userService.queryBasicUserInfo(dto.getAuthor());
        dto.setAuthorName(author.getUserName());
        dto.setAuthorAvatar(author.getPhoto());
        return dto;
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
