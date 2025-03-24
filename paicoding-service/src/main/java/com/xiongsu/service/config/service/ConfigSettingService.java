package com.xiongsu.service.config.service;

import com.xiongsu.api.vo.PageParam;
import com.xiongsu.api.vo.PageVo;
import com.xiongsu.api.vo.banner.ConfigReq;
import com.xiongsu.api.vo.banner.SearchConfigReq;
import com.xiongsu.api.vo.banner.dto.ConfigDTO;

/**
 * Banner后台接口
 */
public interface ConfigSettingService {
    /**
     * 保存
     */
    void saveConfig(ConfigReq configReq);

    /**
     * 删除
     *
     * @param bannerId
     */
    void deleteConfig(Integer bannerId);

    /**
     * 操作（上线/下线）
     *
     * @param bannerId
     */
    void operateConfig(Integer bannerId, Integer pushStatus);

    /**
     * 获取 Banner 列表
     */
    PageVo<ConfigDTO> getConfigList(SearchConfigReq params);

    /**
     * 获取公告列表
     *
     * @param pageParam
     * @return
     */
    PageVo<ConfigDTO> getNoticeList(PageParam pageParam);
}
