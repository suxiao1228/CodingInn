package com.xiongsu.service.article.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiongsu.api.vo.article.dto.TagDTO;
import com.xiongsu.service.article.repository.entity.ArticleTagDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文章标签映mapper接口
 *
 * @author XuYifei
 * @date 2024-07-12
 */
public interface ArticleTagMapper extends BaseMapper<ArticleTagDO> {

    /**
     * 查询文章标签
     *
     * @param articleId
     * @return
     */
    List<TagDTO> listArticleTagDetails(@Param("articleId") Long articleId);



}
