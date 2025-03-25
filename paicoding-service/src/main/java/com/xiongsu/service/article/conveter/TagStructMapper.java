package com.xiongsu.service.article.conveter;

import com.xiongsu.api.vo.article.SearchTagReq;
import com.xiongsu.api.vo.article.TagReq;
import com.xiongsu.api.vo.article.dto.TagDTO;
import com.xiongsu.service.article.repository.entity.TagDO;
import com.xiongsu.service.article.repository.params.SearchTagParams;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface TagStructMapper {
    // instance
    TagStructMapper INSTANCE = Mappers.getMapper( TagStructMapper.class );//获取 TagStructMapper 接口的实现实例，

    // req to params
    @Mapping(source = "pageNumber", target = "pageNum")
    SearchTagParams toSearchParams(SearchTagReq req);

    // do to dto
    @Mapping(source = "id", target = "tagId")
    @Mapping(source = "tagName", target = "tag")
    TagDTO toDTO(TagDO tagDO);

    List<TagDTO> toDTOs(List<TagDO> list);

    @Mapping(source = "tag", target = "tagName")
    TagDO toDO(TagReq tagReq);
}
