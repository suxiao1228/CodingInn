package com.xiongsu.service.config.service;

import com.xiongsu.api.vo.PageVo;
import com.xiongsu.api.vo.config.GlobalConfigReq;
import com.xiongsu.api.vo.config.SearchGlobalConfigReq;
import com.xiongsu.api.vo.config.dto.GlobalConfigDTO;

public interface GlobalConfigService {

    void save(GlobalConfigReq req);

    void delete(Long id);

    PageVo<GlobalConfigDTO> getList(SearchGlobalConfigReq req);
}
