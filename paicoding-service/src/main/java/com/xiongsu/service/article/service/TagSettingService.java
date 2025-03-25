package com.xiongsu.service.article.service;

import com.xiongsu.api.vo.PageVo;
import com.xiongsu.api.vo.article.SearchTagReq;
import com.xiongsu.api.vo.article.TagReq;
import com.xiongsu.api.vo.article.dto.TagDTO;

/**
 * 标签后台接口
 */
public interface TagSettingService {

    /**
     * 标签的新增操作
     * @param tagReq
     */
    void saveTag(TagReq tagReq);

    /**
     * 标签的删除操作
     * @param tagId
     */
    void deleteTag(Integer tagId);

    void operateTag(Integer tagId, Integer pushStatus);

    /**
     * 获取tag接口
     */
    PageVo<TagDTO> getTagList(SearchTagReq req);
}
