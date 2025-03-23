package com.xiongsu.service.article.conveter;

import com.xiongsu.api.vo.article.CategoryReq;
import com.xiongsu.api.vo.article.SearchCategoryReq;
import com.xiongsu.api.vo.article.dto.CategoryDTO;
import com.xiongsu.service.article.repository.entity.CategoryDO;
import com.xiongsu.service.article.repository.params.SearchCategoryParams;
import org.apache.ibatis.annotations.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface CategoryStructMapper {
    // instance
    CategoryStructMapper INSTANCE = Mappers.getMapper( CategoryStructMapper.class );

    // req to params
    @Mapping(source = "pageNumber", target = "pageNum")
    SearchCategoryParams toSearchParams(SearchCategoryReq req);

    // do to dto
    @Mapping(source = "id", target = "categoryId")
    @Mapping(source = "categoryName", target = "category")
    CategoryDTO toDTO(CategoryDO categoryDO);

    List<CategoryDTO> toDTOs(List<CategoryDO> list);

    // req to do
    @Mapping(source = "category", target = "categoryName")
    CategoryDO toDO(CategoryReq categoryReq);
}
