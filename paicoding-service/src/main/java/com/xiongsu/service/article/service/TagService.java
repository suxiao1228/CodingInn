package com.xiongsu.service.article.service;

import com.xiongsu.api.vo.PageParam;
import com.xiongsu.api.vo.PageVo;
import com.xiongsu.api.vo.article.dto.TagDTO;

import java.util.List;

/**
 * 标签Service
 */
public interface TagService {

    PageVo<TagDTO> queryTags(String key, PageParam pageParam);

    Long queryTagId(String tag);

    List<TagDTO> listAllUndeletedTags();

    List<TagDTO> listTagsCategory(Long categoryId);

}
