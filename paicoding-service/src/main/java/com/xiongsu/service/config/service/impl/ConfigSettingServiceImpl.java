package com.xiongsu.service.config.service.impl;

import com.xiongsu.api.enums.YesOrNoEnum;
import com.xiongsu.api.vo.PageParam;
import com.xiongsu.api.vo.PageVo;
import com.xiongsu.api.vo.banner.ConfigReq;
import com.xiongsu.api.vo.banner.SearchConfigReq;
import com.xiongsu.api.vo.banner.dto.ConfigDTO;
import com.xiongsu.core.util.NumUtil;
import com.xiongsu.service.article.repository.params.SearchColumnParams;
import com.xiongsu.service.config.converter.ConfigStructMapper;
import com.xiongsu.service.config.repository.dao.ConfigDao;
import com.xiongsu.service.config.repository.entity.ConfigDO;
import com.xiongsu.service.config.repository.params.SearchConfigParams;
import com.xiongsu.service.config.service.ConfigSettingService;
import jakarta.annotation.Resource;

import java.util.List;

public class ConfigSettingServiceImpl implements ConfigSettingService {

    @Resource
    private ConfigDao configDao;

    @Override
    public void saveConfig(ConfigReq configReq) {
        ConfigDO configDO = ConfigStructMapper.INSTANCE.toDO(configReq);
        if (NumUtil.nullOrZero(configReq.getConfigId())) {
            configDao.save(configDO);
        } else {
            configDO.setId(configReq.getConfigId());
            configDao.updateById(configDO);
        }
    }

    @Override
    public void deleteConfig(Integer configId) {
        ConfigDO configDO = configDao.getById(configId);
        if (configDO != null) {
            configDO.setDeleted(YesOrNoEnum.YES.getCode());
            configDao.updateById(configDO);
        }
    }

    @Override
    public void operateConfig(Integer configId, Integer pushStatus) {
        ConfigDO configDO = configDao.getById(configId);
        if (configDO != null) {
            configDO.setStatus(pushStatus);
            configDao.updateById(configDO);
        }
    }

    @Override
    public PageVo<ConfigDTO> getConfigList(SearchConfigReq req) {
        // 转换
        SearchConfigParams params = ConfigStructMapper.INSTANCE.toSearchParams(req);
        //查询
        List<ConfigDTO> configDTOS = configDao.listBanner(params);
        Long totalCount = configDao.countConfig(params);
        return PageVo.build(configDTOS, params.getPageSize(), params.getPageNum(), totalCount);
    }

    @Override
    public PageVo<ConfigDTO> getNoticeList(PageParam pageParam) {
        List<ConfigDTO> configDTOS = configDao.listNotice(pageParam);
        Integer totalCount = configDao.countNotice();
        return PageVo.build(configDTOS, pageParam.getPageSize(), pageParam.getPageNum(), totalCount);
    }

}
