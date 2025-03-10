package com.xiongsu.service.rank.service;

import com.xiongsu.api.enums.rank.ActivityRankTimeEnum;
import com.xiongsu.api.vo.rank.dto.RankItemDTO;
import com.xiongsu.service.rank.service.model.ActivityScoreBo;

import java.util.List;

public interface UserActivityRankService {

    /**
     * 添加活跃分
     *
     * @param userId
     * @param activityScore
     */
    void addActivityScore(Long userId, ActivityScoreBo activityScore);

    /**
     * 查询用户的活跃信息
     *
     * @param userId
     * @param time
     * @return
     */
    RankItemDTO queryRankInfo(Long userId, ActivityRankTimeEnum time);

    /**
     * 查询活跃度排行榜
     *
     * @param time
     * @return
     */
    List<RankItemDTO> queryRankList(ActivityRankTimeEnum time, int size);
}
