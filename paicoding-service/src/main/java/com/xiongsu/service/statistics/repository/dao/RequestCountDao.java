package com.xiongsu.service.statistics.repository.dao;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiongsu.api.vo.statistics.dto.StatisticsDayDTO;
import com.xiongsu.service.statistics.repository.entity.RequestCountDO;
import com.xiongsu.service.statistics.repository.mapper.RequestCountMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 请求计数
 */

@Repository
public class RequestCountDao extends ServiceImpl<RequestCountMapper, RequestCountDO> {
    public Long getPvTotalCount() {
        return baseMapper.getPvTotalCount();
    }

    /**
     * 获取PV,UV 数据列表
     */
    public List<StatisticsDayDTO> getPvUvDayList(Integer day) {
        return baseMapper.getPvUvDayList(day);
    }
}
