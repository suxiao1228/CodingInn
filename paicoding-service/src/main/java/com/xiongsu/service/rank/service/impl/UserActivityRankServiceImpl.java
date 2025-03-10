package com.xiongsu.service.rank.service.impl;


import cn.hutool.core.util.BooleanUtil;
import com.xiongsu.api.enums.rank.ActivityRankTimeEnum;
import com.xiongsu.api.vo.rank.dto.RankItemDTO;
import com.xiongsu.api.vo.user.dto.SimpleUserInfoDTO;
import com.xiongsu.core.cache.RedisClient;
import com.xiongsu.core.util.DateUtil;
import com.xiongsu.core.util.NumUtil;
import com.xiongsu.service.rank.service.UserActivityRankService;
import com.xiongsu.service.rank.service.model.ActivityScoreBo;
import com.xiongsu.service.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
public class UserActivityRankServiceImpl implements UserActivityRankService {

    private static final String ACTIVITY_SCORE_KEY = "activity_rank_";

    @Autowired
    private UserService userService;

    /**
     * 当天活跃度排行榜
     * @return 当天排行榜Key
     */
    private String todayRankKey() {
        return ACTIVITY_SCORE_KEY + DateUtil.format(DateTimeFormatter.ofPattern("yyyyMMdd"), System.currentTimeMillis());
    }

    /**
     * 本月排行榜
     *
     * @return 月度排行榜key
     */
    private String monthRankKey() {
        return ACTIVITY_SCORE_KEY + DateUtil.format(DateTimeFormatter.ofPattern("yyyyMM"), System.currentTimeMillis());
    }


    /**
     * 添加活跃分
     * @param userId
     * @param activityScore
     */
    @Override
    public void addActivityScore(Long userId, ActivityScoreBo activityScore) {
        if (userId == null) {
            return;
        }

        // 1. 计算活跃度（正为加活跃，负为减活跃）
        String field = "";
        int score = 0;
        if (activityScore.getPath() != null) {
            field = "path_" + activityScore.getPath();
            score = 1;
        } else if (activityScore.getArticleId() != null) {
            field = activityScore.getArticleId() + "_";
            if (activityScore.getPraise() != null) {
                field += "praise";
                score = BooleanUtils.isTrue(activityScore.getPraise()) ? 2 : -2;
            } else if (activityScore.getCollect() != null) {
                field += "collect";
                score = BooleanUtils.isTrue(activityScore.getCollect()) ? 2 : -2;
            } else if (activityScore.getRate() != null) {
                // 评论回复
                field += "rate";
                score = BooleanUtils.isTrue(activityScore.getRate()) ? 3 : -3;
            } else if (BooleanUtils.isTrue(activityScore.getPublishArticle())) {
                // 发布文章
                field += "publish";
                score += 10;
            }
        } else if (activityScore.getFollowedUserId() != null) {
            field += activityScore.getFollowedUserId() + "_follow";
            score = BooleanUtils.isTrue(activityScore.getFollow()) ? 2 : -2;
        } else {
            return;
        }

        final String todayRankKey = todayRankKey();
        final String monthRankKey = monthRankKey();

        //2.幂等，判断之前是否更新过相关的活跃度信息
        final String userActionKey = ACTIVITY_SCORE_KEY + userId + DateUtil.format(DateTimeFormatter.ofPattern("yyyyMMdd"), System.currentTimeMillis());
        Integer ans = RedisClient.hGet(userActionKey, field, Integer.class);
        if (ans == null) {
            // 2.1 之前没有加分记录，执行具体的加分
            if (score > 0) {
                // 记录加分记录
                RedisClient.hSet(userActionKey, field, score);
                // 个人用户的操作记录，保存一个月的有效期，方便用户查询自己最近31天的活跃情况
                RedisClient.expire(userActionKey, 31 * DateUtil.ONE_DAY_MILL);

                //更新当天和当月的用户排行榜
                Double newAns = RedisClient.zIncrBy(todayRankKey, String.valueOf(userId), score);
                RedisClient.zIncrBy(monthRankKey, String.valueOf(userId), score);
                if (log.isDebugEnabled()) {
                    log.info("活跃度更新加分! key#field = {}#{}, add = {}, newScore = {}", todayRankKey, userId, score, newAns);
                }
                if (newAns <= score) {

                    Long ttl = RedisClient.ttl(todayRankKey);
                    if (!NumUtil.upZero(ttl)) {
                        RedisClient.expire(todayRankKey, 31 * DateUtil.ONE_DAY_MILL);
                    }
                    ttl = RedisClient.ttl(monthRankKey);
                    if (!NumUtil.upZero(ttl)) {
                        RedisClient.expire(monthRankKey, 12 * DateUtil.ONE_MONTH_SECONDS);
                    }
                }
            }
        } else if (ans > 0) {
            // 2.2 之前已经加过分，因此这次减分可以执行
            if (score > 0) {
                // 移除用户的活跃执行记录 --> 即移除用来做防重复添加活跃度的幂等键
                Boolean oldHave = RedisClient.hDel(userActionKey, field);
                if (BooleanUtils.isTrue(oldHave)) {
                    Double newAns = RedisClient.zIncrBy(todayRankKey, String.valueOf(userId), score);
                    RedisClient.zIncrBy(monthRankKey, String.valueOf(userId), score);
                    if (log.isDebugEnabled()) {
                        log.info("活跃度更新减分! key#field = {}#{}, add = {}, newScore = {}", todayRankKey, userId, score, newAns);
                    }
                }
            }
        }
    }

    @Override
    public RankItemDTO queryRankInfo(Long userId, ActivityRankTimeEnum time) {
        RankItemDTO item = new RankItemDTO();
        item.setUser(userService.querySimpleUserInfo(userId));

        String rankKey = time == ActivityRankTimeEnum.DAY ? todayRankKey() : monthRankKey();
        ImmutablePair<Integer, Double> rank = RedisClient.zRankInfo(rankKey, String.valueOf(userId));
        item.setRank(rank.getLeft());
        item.setScore(rank.getRight().intValue());
        return item;
    }

    @Override
    public List<RankItemDTO> queryRankList(ActivityRankTimeEnum time, int size) {
        String rankKey = time == ActivityRankTimeEnum.DAY ? todayRankKey() : monthRankKey();
        // 1. 获取topN的活跃用户
        List<ImmutablePair<String, Double>> rankList = RedisClient.zTopNScore(rankKey, size);
        if(CollectionUtils.isEmpty(rankList)) {
            return Collections.emptyList();
        }

        // 2.查询用户对应的基本信息
        // 构建userId -> 活跃评分的map映射，用于补齐用户信息
        Map<Long, Integer> userScoreMap = rankList.stream().collect(Collectors.toMap(s -> Long.valueOf(s.getLeft()), s->s.getRight().intValue()));
        List<SimpleUserInfoDTO> users = userService.batchQuerySimpleUserInfo(userScoreMap.keySet());

        //3.根据评分进行排序
        List<RankItemDTO> rank = users.stream()
                .map(user -> new RankItemDTO().setUser(user).setScore(userScoreMap.getOrDefault(user.getUserId(), 0)))
                .sorted((o1, o2) -> Integer.compare(o2.getScore(), o1.getScore()))
                .collect(Collectors.toList());

        // 4. 补齐每个用户的排名
        IntStream.range(0, rank.size()).forEach(i -> rank.get(i).setRank(i + 1));
        return rank;
    }
}
