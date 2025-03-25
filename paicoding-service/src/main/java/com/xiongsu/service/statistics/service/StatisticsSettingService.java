package com.xiongsu.service.statistics.service;

import com.xiongsu.api.vo.statistics.dto.StatisticsCountDTO;
import com.xiongsu.api.vo.statistics.dto.StatisticsDayDTO;

import java.util.List;

/**
 * 数据统计后台接口
 */
public interface StatisticsSettingService {


    /**
     * 获取总数
     *
     * @return
     */
    StatisticsCountDTO getStatisticsCount();

    /**
     * 获取每天的PV UV统计数据
     *
     * @param day
     * @return
     */
    List<StatisticsDayDTO> getPvUvDayList(Integer day);
}
