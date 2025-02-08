package com.xiongsu.service.article.service.impl;


import com.xiongsu.api.vo.PageVo;
import com.xiongsu.api.vo.article.SearchTagReq;
import com.xiongsu.api.vo.article.TagReq;
import com.xiongsu.api.vo.article.dto.TagDTO;
import com.xiongsu.service.article.repository.dao.TagDao;
import com.xiongsu.service.article.service.TagSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TagSettingServiceImpl implements TagSettingService {

    private static final String CACHE_TAG_PRE = "cache_tag_pre_";

    private static final Long CACHE_TAG_EXPRIE_TIME = 100L;

    @Autowired
    private TagDao tagDao;

    @Override
    public void saveTag(TagReq tagReq) {

    }

    @Override
    public void deleteTag(Integer tagId) {

    }

    @Override
    public void operateTag(Integer tagId, Integer pushStatus) {

    }

    @Override
    public PageVo<TagDTO> getTagList(SearchTagReq req) {
        return null;
    }

    @Override
    public TagDTO getTagById(Long tagId) {
        return null;
    }
}
