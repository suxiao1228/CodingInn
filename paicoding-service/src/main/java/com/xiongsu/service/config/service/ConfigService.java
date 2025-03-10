package com.xiongsu.service.config.service;

import com.xiongsu.api.enums.ConfigTypeEnum;
import com.xiongsu.api.vo.banner.dto.ConfigDTO;

import java.util.List;

/**
 * Banner前台接口
 */
public interface ConfigService {


    /**
     * 获取Banner列表
     */
    List<ConfigDTO> getConfigList(ConfigTypeEnum configTypeEnum);

    /**
     * 阅读次数+1
     */
    void updateVisit(long configId, String extra);
}
