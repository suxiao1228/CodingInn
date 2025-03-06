package com.xiongsu.web.controller.article.vo;

import com.xiongsu.api.vo.PageListVo;
import com.xiongsu.api.vo.article.dto.ArticleDTO;
import lombok.Data;

@Data
public class ArticleListVo {
    /**
     * 归档类型
     */
    private String archives;
    /**
     * 归档id
     */
    private Long archiveId;

    private PageListVo<ArticleDTO> articles;
}
