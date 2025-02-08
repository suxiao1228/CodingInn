package com.xiongsu.service.article.conveter;

import com.xiongsu.api.vo.article.ColumnArticleReq;
import com.xiongsu.api.vo.article.SearchColumnArticleReq;
import com.xiongsu.service.article.repository.entity.ColumnArticleDO;
import com.xiongsu.service.article.repository.params.ColumnArticleParams;
import com.xiongsu.service.article.repository.params.SearchColumnArticleParams;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ColumnArticleStructMapper {
    ColumnArticleStructMapper INSTANCE = Mappers.getMapper( ColumnArticleStructMapper.class );

    SearchColumnArticleParams toSearchParams(SearchColumnArticleReq req);

    ColumnArticleParams toParams(ColumnArticleReq req);

    ColumnArticleDO reqToDO(ColumnArticleReq req);
}
