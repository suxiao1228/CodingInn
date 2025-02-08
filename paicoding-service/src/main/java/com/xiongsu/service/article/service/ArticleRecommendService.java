package com.xiongsu.service.article.service;

import com.xiongsu.api.vo.PageListVo;
import com.xiongsu.api.vo.PageParam;
import com.xiongsu.api.vo.article.dto.ArticleDTO;

public interface ArticleRecommendService {
    /**
     * 文章关联推荐
     */

    PageListVo<ArticleDTO> relatedRecommend(Long article, PageParam pageParam);

}
