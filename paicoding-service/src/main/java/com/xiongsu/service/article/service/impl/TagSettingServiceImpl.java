package com.xiongsu.service.article.service.impl;

import com.xiongsu.api.vo.PageVo;
import com.xiongsu.api.vo.article.SearchTagReq;
import com.xiongsu.api.vo.article.TagReq;
import com.xiongsu.api.vo.article.dto.TagDTO;
import com.xiongsu.core.cache.RedisClient;
import com.xiongsu.core.util.NumUtil;
import com.xiongsu.service.article.conveter.TagStructMapper;
import com.xiongsu.service.article.repository.dao.TagDao;
import com.xiongsu.service.article.repository.entity.TagDO;
import com.xiongsu.service.article.repository.params.SearchTagParams;
import com.xiongsu.service.article.service.TagSettingService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TagSettingServiceImpl implements TagSettingService {

    private static final String CACHE_TAG_PRE = "cache_tag_pre_";

    private static final Long CACHE_TAG_EXPRIE_TIME = 100L;

    @Resource
    private TagDao tagDao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveTag(TagReq tagReq) {
        TagDO tagDO = TagStructMapper.INSTANCE.toDO(tagReq);

        //先写MySQL
        if (NumUtil.nullOrZero(tagReq.getTagId())) {
            tagDao.save(tagDO);
        } else {
            tagDO.setId(tagReq.getTagId());
            tagDao.updateById(tagDO);
        }

        //再删除Redis
        String redisKey = CACHE_TAG_PRE + tagDO.getId();
        RedisClient.del(redisKey);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTag(Integer tagId) {
        TagDO tagDO = tagDao.getById(tagId);
        if (tagDO != null) {
            // 先写MySQL
            tagDao.removeById(tagId);

            //再删除Redis
            String redisKey = CACHE_TAG_PRE + tagDO.getId();
            RedisClient.del(redisKey);
        }
    }

    @Override
    public void operateTag(Integer tagId, Integer pushStatus) {
        TagDO tagDO = tagDao.getById(tagId);
        if (tagDO != null) {
            // 先写MySQL
            tagDO.setStatus(pushStatus);
            tagDao.updateById(tagDO);

            //再删除Redis
            String redisKey = CACHE_TAG_PRE + tagDO.getId();
            RedisClient.del(redisKey);
        }
    }

    @Override
    public PageVo<TagDTO> getTagList(SearchTagReq req) {
        // 转换
        SearchTagParams params = TagStructMapper.INSTANCE.toSearchParams(req);
        // 查询
        List<TagDTO> tagDTOS = TagStructMapper.INSTANCE.toDTOs(tagDao.listTag(params));
        Long totalCount = tagDao.countTag(params);
        return PageVo.build(tagDTOS, params.getPageSize(), params.getPageNum(), totalCount);
    }
}
