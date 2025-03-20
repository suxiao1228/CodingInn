package com.xiongsu.web.controller.search.vo;

import com.xiongsu.api.vo.article.dto.SimpleArticleDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Tag(name = "文章信息")
public class SearchArticleVo implements Serializable {
    private static final long serialVersionUID = -2989169905031769195L;

    @Schema(description = "搜索的关键词")
    private String key;

    @Schema(description = "文章列表")
    private List<SimpleArticleDTO> items;
}
