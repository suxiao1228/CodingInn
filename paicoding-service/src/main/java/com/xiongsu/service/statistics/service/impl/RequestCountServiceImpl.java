package com.xiongsu.service.statistics.service.impl;

import com.xiongsu.api.vo.statistics.dto.StatisticsDayDTO;
import com.xiongsu.service.statistics.repository.dao.RequestCountDao;
import com.xiongsu.service.statistics.repository.entity.RequestCountDO;
import com.xiongsu.service.statistics.service.RequestCountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.List;

@Service
public class RequestCountServiceImpl implements RequestCountService {
    @Autowired
    private RequestCountDao requestCountDao;

    @Override
    public RequestCountDO getRequestCount(String host) {
        return null;
    }

    @Override
    public List<RequestCountDO> getTodayRequestCountList() {
        return List.of();
    }

    @Override
    public void insert(String host) {

    }

    @Override
    public boolean insertAndSetCount(String host, Integer count, Date date) {
        return false;
    }

    @Override
    public void insertOrUpdateBatch(List<RequestCountDO> requestCountDOList) {

    }

    @Override
    public void incrementCount(Long id) {

    }

    @Override
    public Long getPvTotalCount() {
        return requestCountDao.getPvTotalCount();
    }

    @Override
    public List<StatisticsDayDTO> getPvUvDayList(Integer day) {
        return requestCountDao.getPvUvDayList(day);
    }
}
