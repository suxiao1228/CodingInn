package com.xiongsu.service.config.converter;


import com.xiongsu.api.vo.banner.ConfigReq;
import com.xiongsu.api.vo.banner.SearchConfigReq;
import com.xiongsu.api.vo.banner.dto.ConfigDTO;
import com.xiongsu.api.vo.config.GlobalConfigReq;
import com.xiongsu.api.vo.config.SearchGlobalConfigReq;
import com.xiongsu.api.vo.config.dto.GlobalConfigDTO;
import com.xiongsu.service.config.repository.entity.ConfigDO;
import com.xiongsu.service.config.repository.entity.GlobalConfigDO;
import com.xiongsu.service.config.repository.params.SearchConfigParams;
import com.xiongsu.service.config.repository.params.SearchGlobalConfigParams;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ConfigStructMapper {
    // instance
    ConfigStructMapper INSTANCE = Mappers.getMapper( ConfigStructMapper.class );

    // req to params
    @Mapping(source = "pageNumber", target = "pageNum")
    SearchConfigParams toSearchParams(SearchConfigReq req);

    // req to params
    @Mapping(source = "pageNumber", target = "pageNum")
    // key to keywords
    @Mapping(source = "keywords", target = "key")
    SearchGlobalConfigParams toSearchGlobalParams(SearchGlobalConfigReq req);

    // do to dto
    ConfigDTO toDTO(ConfigDO configDO);

    List<ConfigDTO> toDTOS(List<ConfigDO> configDOS);

    ConfigDO toDO(ConfigReq configReq);

    // do to dto
    // key to keywords
    @Mapping(source = "key", target = "keywords")
    GlobalConfigDTO toGlobalDTO(GlobalConfigDO configDO);

    List<GlobalConfigDTO> toGlobalDTOS(List<GlobalConfigDO> configDOS);

    @Mapping(source = "keywords", target = "key")
    GlobalConfigDO toGlobalDO(GlobalConfigReq req);
}
