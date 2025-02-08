package com.xiongsu.service.article.service;

import com.xiongsu.api.vo.PageParam;
import com.xiongsu.api.vo.PageVo;
import com.xiongsu.api.vo.article.dto.TagDTO;

/**
 * 标签Service
 */
public interface TagService {


    Long queryTagId(String tag);

    PageVo<TagDTO> queryTags(String key, PageParam pageParam);
}
