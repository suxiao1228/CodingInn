package com.xiongsu.service.article.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xiongsu.api.enums.ArticleEventEnum;
import com.xiongsu.api.enums.OperateArticleEnum;
import com.xiongsu.api.enums.PushStatusEnum;
import com.xiongsu.api.enums.YesOrNoEnum;
import com.xiongsu.api.event.ArticleMsgEvent;
import com.xiongsu.api.exception.ExceptionUtil;
import com.xiongsu.api.vo.PageVo;
import com.xiongsu.api.vo.article.ArticlePostReq;
import com.xiongsu.api.vo.article.SearchArticleReq;
import com.xiongsu.api.vo.article.dto.ArticleAdminDTO;
import com.xiongsu.api.vo.constants.StatusEnum;
import com.xiongsu.core.util.SpringUtil;
import com.xiongsu.service.article.conveter.ArticleStructMapper;
import com.xiongsu.service.article.repository.dao.ArticleDao;
import com.xiongsu.service.article.repository.dao.ColumnArticleDao;
import com.xiongsu.service.article.repository.entity.ArticleDO;
import com.xiongsu.service.article.repository.entity.ColumnArticleDO;
import com.xiongsu.service.article.repository.params.SearchArticleParams;
import com.xiongsu.service.article.service.ArticleSettingService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 提供了文章的增删改查
 */
@Service
public class ArticleSettingServiceImpl implements ArticleSettingService {

    @Autowired
    private ArticleDao articleDao;

    @Autowired
    private ColumnArticleDao columnArticleDao;


    @Override
    @CacheEvict(key = "'sideBar_' + #req.articleId", cacheManager = "caffeineCacheManager", cacheNames = "article")
    public void updateArticle(ArticlePostReq req) {
            if(req.getStatus() != PushStatusEnum.OFFLINE.getCode()
                    && req.getStatus() != PushStatusEnum.ONLINE.getCode()
                    && req.getStatus() != PushStatusEnum.REVIEW.getCode()
            ) {
                throw ExceptionUtil.of(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "发布状态不合法!");
            }
            ArticleDO article = articleDao.getById(req.getArticleId());
            if (article == null) {
                throw ExceptionUtil.of(StatusEnum.RECORDS_NOT_EXISTS, "文章不存在!");
            }
            if (StringUtils.isNotBlank(req.getTitle())) {
                article.setTitle(req.getTitle());
            }
            if (StringUtils.isNotBlank(req.getShortTitle())) {
                article.setShortTitle(req.getShortTitle());
            }
            ArticleEventEnum operateEvent = null;
            if (req.getStatus() != null) {
                article.setStatus(req.getStatus());
                if (req.getStatus() == PushStatusEnum.OFFLINE.getCode()) {
                    operateEvent = ArticleEventEnum.OFFLINE;
                } else if (req.getStatus() == PushStatusEnum.REVIEW.getCode()) {
                    operateEvent = ArticleEventEnum.REVIEW;
                } else if (req.getStatus() == PushStatusEnum.ONLINE.getCode()) {
                    operateEvent = ArticleEventEnum.ONLINE;
                }
            }
            articleDao.updateById(article);
        if (operateEvent != null) {
            // 发布文章待审核、上线、下线事件
            SpringUtil.publishEvent(new ArticleMsgEvent<>(this, operateEvent, article));
        }
    }

    @Override
    public void deleteArticle(Long articleId) {
        ArticleDO dto = articleDao.getById(articleId);
        if(dto != null && dto.getDeleted() != YesOrNoEnum.YES.getCode()) {
            // 查询该文章是否关联了教程，如果已经关联了教程，则不能删除
            long count = columnArticleDao.count(
                    Wrappers.<ColumnArticleDO>lambdaQuery().eq(ColumnArticleDO::getArticleId, articleId));

            if (count > 0) {
                throw ExceptionUtil.of(StatusEnum.ARTICLE_RELATION_TUTORIAL, articleId, "请先解除文章与教程的关联关系");
            }

            dto.setDeleted(YesOrNoEnum.YES.getCode());
            articleDao.updateById(dto);

            // 发布文章删除事件
            SpringUtil.publishEvent(new ArticleMsgEvent<>(this, ArticleEventEnum.DELETE, dto));
        }else{
            throw ExceptionUtil.of(StatusEnum.ARTICLE_NOT_EXISTS, articleId);
        }
    }

    /**
     * 设置文章状态
     * @param articleId
     * @param operate
     */
    @Override
    public void operateArticle(Long articleId, OperateArticleEnum operate) {
        ArticleDO articleDO = articleDao.getById(articleId);
        if (articleDO == null) {
            throw ExceptionUtil.of(StatusEnum.ARTICLE_NOT_EXISTS, articleId);
        }
        setArticleStat(articleDO, operate);
        articleDao.updateById(articleDO);
    }

    private void setArticleStat(ArticleDO articleDO, OperateArticleEnum operate) {
        switch (operate) {
            case OFFICAL:
            case CANCEL_OFFICAL:
                compareAndUpdate(articleDO::getOfficalStat, articleDO::setOfficalStat, operate.getDbStatCode());
                return;
            case TOPPING:
            case CANCEL_TOPPING:
                compareAndUpdate(articleDO::getToppingStat, articleDO::setToppingStat, operate.getDbStatCode());
                return;
            case CREAM:
            case CANCEL_CREAM:
                compareAndUpdate(articleDO::getCreamStat, articleDO::setCreamStat, operate.getDbStatCode());
                return;
            default:
        }
    }

    /**
     * 相同则直接返回false不用更新；不同则更新,返回true
     *
     * @param <T>
     * @param supplier
     * @param consumer
     * @param input
     */
    private <T> void compareAndUpdate(Supplier<T> supplier, Consumer<T> consumer, T input) {
        if (Objects.equals(supplier.get(), input)) {
            return;
        }
        consumer.accept(input);
    }

    @Override
    public PageVo<ArticleAdminDTO> getArticleList(SearchArticleReq req) {
        //转换参数，从前端获取的参数转换为数据库查询参数
        SearchArticleParams searchArticleParams = ArticleStructMapper.INSTANCE.toSearchParams(req);

        //查询文章列表，分页
        List<ArticleAdminDTO> articleDTOS = articleDao.listArticlesByParams(searchArticleParams);

        //查询文章总数
        Long totalCount = articleDao.countArticleByParams(searchArticleParams);
        return PageVo.build(articleDTOS, req.getPageSize(), req.getPageNumber(), totalCount);
    }
}
