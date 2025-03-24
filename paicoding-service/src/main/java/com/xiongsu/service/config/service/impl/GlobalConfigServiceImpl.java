package com.xiongsu.service.config.service.impl;

import com.xiongsu.api.event.ConfigRefreshEvent;
import com.xiongsu.api.exception.ExceptionUtil;
import com.xiongsu.api.vo.PageVo;
import com.xiongsu.api.vo.config.GlobalConfigReq;
import com.xiongsu.api.vo.config.SearchGlobalConfigReq;
import com.xiongsu.api.vo.config.dto.GlobalConfigDTO;
import com.xiongsu.api.vo.constants.StatusEnum;
import com.xiongsu.core.util.NumUtil;
import com.xiongsu.core.util.SpringUtil;
import com.xiongsu.service.config.converter.ConfigStructMapper;
import com.xiongsu.service.config.repository.dao.ConfigDao;
import com.xiongsu.service.config.repository.entity.GlobalConfigDO;
import com.xiongsu.service.config.repository.params.SearchGlobalConfigParams;
import com.xiongsu.service.config.service.GlobalConfigService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GlobalConfigServiceImpl implements GlobalConfigService {

    @Resource
    private ConfigDao configDao;

    @Override
    public void save(GlobalConfigReq req) {
        GlobalConfigDO globalConfigDO = ConfigStructMapper.INSTANCE.toGlobalDO(req);
        //id不为空
        if (NumUtil.nullOrZero(globalConfigDO.getId())) {
            configDao.save(globalConfigDO);
        } else {
            configDao.updateById(globalConfigDO);
        }

        //配置更新之后，主动出发配置的动态加载
        SpringUtil.publishEvent(new ConfigRefreshEvent(this, req.getKeywords(), req.getValue()));
    }

    @Override
    public void delete(Long id) {
        GlobalConfigDO globalConfigDO = configDao.getGlobalConfigById(id);
        if (globalConfigDO != null) {
            configDao.delete(globalConfigDO);
        } else {
            throw ExceptionUtil.of(StatusEnum.RECORDS_NOT_EXISTS, "记录不存在");
        }
    }

    @Override
    public PageVo<GlobalConfigDTO> getList(SearchGlobalConfigReq req) {
        ConfigStructMapper mapper = ConfigStructMapper.INSTANCE;
        // 转换
        SearchGlobalConfigParams params = mapper.toSearchGlobalParams(req);
        // 查询
        List<GlobalConfigDO> list = configDao.listGlobalConfig(params);
        // 总数
        Long total = configDao.countGlobalConfig(params);

        return PageVo.build(mapper.toGlobalDTOS(list), params.getPageSize(), params.getPageNum(), total);
    }
}
