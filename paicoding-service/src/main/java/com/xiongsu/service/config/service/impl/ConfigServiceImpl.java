package com.xiongsu.service.config.service.impl;

import com.xiongsu.api.enums.ConfigTypeEnum;
import com.xiongsu.api.vo.banner.dto.ConfigDTO;
import com.xiongsu.service.config.repository.dao.ConfigDao;
import com.xiongsu.service.config.service.ConfigService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class ConfigServiceImpl implements ConfigService {

    @Resource
    private ConfigDao configDao;

    @Override
    public List<ConfigDTO> getConfigList(ConfigTypeEnum configTypeEnum) {
        return configDao.listConfigByType(configTypeEnum.getCode());
    }

    /**
     * 配置发生变更之后，失效本地缓存，这里主要是配合 SidebarServiceImpl 中的缓存使用
     *
     * @param configId
     * @param extra
     */
    @Override
    public void updateVisit(long configId, String extra) {
        configDao.updatePdfConfigVisitNum(configId, extra);
    }
}
