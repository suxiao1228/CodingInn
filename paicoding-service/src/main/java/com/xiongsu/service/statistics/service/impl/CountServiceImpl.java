package com.xiongsu.service.statistics.service.impl;

import com.xiongsu.api.vo.user.dto.ArticleFootCountDTO;
import com.xiongsu.api.vo.user.dto.UserStatisticInfoDTO;
import com.xiongsu.core.cache.RedisClient;
import com.xiongsu.service.article.repository.dao.ArticleDao;
import com.xiongsu.service.statistics.constants.CountConstants;
import com.xiongsu.service.statistics.service.CountService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 计数服务，后续计数相关的可以考虑基于redis来做
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Slf4j
@Service
public class CountServiceImpl implements CountService {
    @Resource
    private ArticleDao articleDao;

    @Override
    public ArticleFootCountDTO queryArticleCountInfoByArticleId(Long articleId) {
        return null;
    }

    @Override
    public ArticleFootCountDTO queryArticleCountInfoByUserId(Long userId) {
        return null;
    }

    @Override
    public Long queryCommentPraiseCount(Long commentId) {
        return 0L;
    }

    @Override
    public UserStatisticInfoDTO queryUserStatisticInfo(Long userId) {
        Map<String, Integer> ans = RedisClient.hGetAll(CountConstants.USER_STATISTIC_INFO + userId, Integer.class);
        UserStatisticInfoDTO info = new UserStatisticInfoDTO();
        info.setFollowCount(ans.getOrDefault(CountConstants.FOLLOW_COUNT, 0));
        info.setArticleCount(ans.getOrDefault(CountConstants.ARTICLE_COUNT, 0));
        info.setPraiseCount(ans.getOrDefault(CountConstants.PRAISE_COUNT, 0));
        info.setCollectionCount(ans.getOrDefault(CountConstants.COLLECTION_COUNT, 0));
        info.setReadCount(ans.getOrDefault(CountConstants.READ_COUNT, 0));
        info.setFansCount(ans.getOrDefault(CountConstants.FANS_COUNT, 0));
        return info;
    }

    @Override
    public ArticleFootCountDTO queryArticleStatisticInfo(Long articleId) {
        return null;
    }

    @Override
    public void incrArticleReadCount(Long authorUserId, Long articleId) {
        //db层的计数+1
        articleDao.incrReadCount(articleId);
        //redis的计数器 + 1
        RedisClient.pipelineAction()
                .add(CountConstants.ARTICLE_STATISTIC_INFO + articleId, CountConstants.READ_COUNT,
                        (connection, key, value) -> connection.hIncrBy(key, value, 1))
                .add(CountConstants.USER_STATISTIC_INFO + authorUserId, CountConstants.READ_COUNT,
                        (connection, key, value) -> connection.hIncrBy(key, value, 1))
                .execute();
    }

    @Override
    public void refreshUserStatisticInfo(Long userId) {

    }

    @Override
    public void refreshArticleStatisticInfo(Long articleId) {

    }
}
