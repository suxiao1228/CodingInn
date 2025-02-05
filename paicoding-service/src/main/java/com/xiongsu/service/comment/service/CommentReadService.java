package com.xiongsu.service.comment.service;

import com.xiongsu.api.vo.PageParam;
import com.xiongsu.api.vo.comment.dto.TopCommentDTO;
import com.xiongsu.service.comment.repository.entity.CommentDO;

import java.util.List;

public interface CommentReadService {


    /**
     * 根据评论id查询评论信息
     */
    CommentDO queryComment(Long commentId);

    /**
     * 查询文章评论列表
     * @param articleId
     * @param pageParam
     * @return
     */
    List<TopCommentDTO> getArticleComments(Long articleId, PageParam pageParam);

    /**
     * 查询热门评论
     */
    TopCommentDTO queryHotComment(Long articleId);

    /**
     * 文章的有效评论数
     */
    int queryCommentCount(Long articleId);
}
