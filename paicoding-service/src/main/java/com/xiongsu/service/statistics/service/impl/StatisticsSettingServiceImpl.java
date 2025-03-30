package com.xiongsu.service.statistics.service.impl;

import com.xiongsu.api.vo.statistics.dto.StatisticsCountDTO;
import com.xiongsu.api.vo.statistics.dto.StatisticsDayDTO;
import com.xiongsu.api.vo.user.dto.UserFootStatisticDTO;
import com.xiongsu.core.cache.RedisClient;
import com.xiongsu.service.article.service.ArticleReadService;
import com.xiongsu.service.article.service.ColumnService;
import com.xiongsu.service.statistics.service.RequestCountService;
import com.xiongsu.service.statistics.service.StatisticsSettingService;
import com.xiongsu.service.user.service.UserFootService;
import com.xiongsu.service.user.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

/**
 * 数据统计后台接口
 */

@Slf4j
@Service
public class StatisticsSettingServiceImpl implements StatisticsSettingService {

    @Autowired
    private RequestCountService requestCountService;

    @Autowired
    private UserService userService;

    @Autowired
    private ColumnService columnService;

    @Autowired
    private UserFootService userFootService;

    @Autowired
    private ArticleReadService articleReadService;

    @Resource
    private AiConfig aiConfig;

    @Override
    public void saveRequestCount(String host) {

        Integer count = RedisClient.hGet(RequestCountService.REQUEST_COUNT_PREFIX + Date.valueOf(LocalDate.now()), host, Integer.class);
        if(count != null){
            RedisClient.hSet(RequestCountService.REQUEST_COUNT_PREFIX + Date.valueOf(LocalDate.now()), host, count);
        }else{
            RedisClient.hSet(RequestCountService.REQUEST_COUNT_PREFIX + Date.valueOf(LocalDate.now()), host, 1);
        }

        // 以下是直接访问DB的逻辑，要操作两次数据库，访问压力太大
//        RequestCountDO requestCountDO = requestCountService.getRequestCount(host);
//        if (requestCountDO == null) {
//            requestCountService.insert(host);
//        } else {
//            // 改为数据库直接更新
//            requestCountService.incrementCount(requestCountDO.getId());
//        }
    }

    @Override
    public StatisticsCountDTO getStatisticsCount() {
        //从 user_foot 表中查询点赞数，收藏数，留言数，阅读数
        UserFootStatisticDTO userFootStatisticDTO = userFootService.getFootCount();
        if (userFootStatisticDTO == null) {
            userFootStatisticDTO = new UserFootStatisticDTO();
        }
        return StatisticsCountDTO.builder()
                .userCount(userService.getUserCount())//从数据库中查询用户总数
                .articleCount(articleReadService.getArticleCount())//查询文章总数量
                .pvCount(requestCountService.getPvTotalCount())//查询页面浏览量
                .tutorialCount(columnService.getTutorialCount())//统计专栏文章的数量
                .commentCount(userFootStatisticDTO.getCommentCount())//赋值到DTO
                .collectCount(userFootStatisticDTO.getCollectionCount())//用户收藏的数量
                .likeCount(userFootStatisticDTO.getPraiseCount())//用户点赞的数量
                .readCount(userFootStatisticDTO.getReadCount())//统计用户的阅读量
                .starPayCount(aiConfig.getMaxNum().getStarNumber())
                .build();
    }

    @Override
    public List<StatisticsDayDTO> getPvUvDayList(Integer day) {
        return requestCountService.getPvUvDayList(day);
    }


}
