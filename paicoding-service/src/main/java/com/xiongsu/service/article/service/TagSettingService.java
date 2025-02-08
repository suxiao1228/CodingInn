package com.xiongsu.service.article.service;


import com.xiongsu.api.vo.PageVo;
import com.xiongsu.api.vo.article.SearchTagReq;
import com.xiongsu.api.vo.article.TagReq;
import com.xiongsu.api.vo.article.dto.TagDTO;

/**
 * 标签后台接口
 */
public interface TagSettingService {

    void saveTag(TagReq tagReq);

    void deleteTag(Integer tagId);

    void operateTag(Integer tagId, Integer pushStatus);

    /**
     * 获取tag列表
     *
     * @param req
     * @return
     */
    PageVo<TagDTO> getTagList(SearchTagReq req);

    TagDTO getTagById(Long tagId);
}
