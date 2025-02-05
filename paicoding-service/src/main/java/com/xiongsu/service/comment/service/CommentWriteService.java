package com.xiongsu.service.comment.service;

import com.xiongsu.api.vo.comment.CommentSaveReq;

public interface CommentWriteService {
    /**
     * 更新/保存评论
     */
    Long saveComment(CommentSaveReq commentSaveReq);


    /**
     * 删除评论
     */
    void deleteComment(Long commentId, Long userId);
}
