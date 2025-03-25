package com.xiongsu.service.statistics.service;

import com.xiongsu.api.vo.statistics.dto.StatisticsDayDTO;
import com.xiongsu.service.statistics.repository.entity.RequestCountDO;

import java.sql.Date;
import java.util.List;

public interface RequestCountService {

    public static final String REQUEST_COUNT_PREFIX = "request_count_";

    RequestCountDO getRequestCount(String host);

    List<RequestCountDO> getTodayRequestCountList();

    void insert(String host);

    boolean insertAndSetCount(String host, Integer count, Date date);

    void insertOrUpdateBatch(List<RequestCountDO> requestCountDOList);

    void incrementCount(Long id);

    Long getPvTotalCount();

    List<StatisticsDayDTO> getPvUvDayList(Integer day);
}
