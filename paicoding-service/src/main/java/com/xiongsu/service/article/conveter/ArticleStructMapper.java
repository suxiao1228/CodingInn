package com.xiongsu.service.article.conveter;

import com.xiongsu.api.vo.article.SearchArticleReq;
import com.xiongsu.service.article.repository.params.SearchArticleParams;
import org.apache.ibatis.annotations.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ArticleStructMapper {
    ArticleStructMapper INSTANCE = Mappers.getMapper( ArticleStructMapper.class );

    //指定字段映射关系：
    //source = "pageNumber"：来源字段，表示 SearchArticleReq 类中的 pageNumber。
    //target = "pageNum"：目标字段，表示 SearchArticleParams 类中的 pageNum。
    //这表明：
    //SearchArticleReq.pageNumber → SearchArticleParams.pageNum
    //其他未声明的字段（如果名称相同）会 自动映射。
    @Mapping(source = "pageNumber", target = "pageNum")
    SearchArticleParams toSearchParams(SearchArticleReq req);
}
