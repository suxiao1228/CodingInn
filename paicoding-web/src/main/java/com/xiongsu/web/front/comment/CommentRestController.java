package com.xiongsu.web.front.comment;


import com.xiongsu.api.vo.PageParam;
import com.xiongsu.api.vo.ResVo;
import com.xiongsu.api.vo.comment.dto.TopCommentDTO;
import com.xiongsu.api.vo.constants.StatusEnum;
import com.xiongsu.core.util.NumUtil;
import com.xiongsu.service.comment.service.CommentReadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path="comment/api")
public class CommentRestController {

    @Autowired
    private CommentReadService commentReadService;

    /**
     * 评论列表页
     */
    @ResponseBody
    @RequestMapping(path = "list")//获取某篇文章的顶级评论
    public ResVo<List<TopCommentDTO>> list (Long articleId, Long pageNum, Long pageSize) {
        if(NumUtil.nullOrZero(articleId)) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "文章id为空");
        }
        pageNum = Optional.ofNullable(pageNum).orElse(PageParam.DEFAULT_PAGE_NUM); // 分页参数，表示当前页码
        pageSize = Optional.ofNullable(pageSize).orElse(PageParam.DEFAULT_PAGE_SIZE); // 分页参数，表示当前页返回多少数据
        List<TopCommentDTO> result = commentReadService.getArticleComments(articleId, PageParam.newPageInstance(pageNum, pageSize));
        return ResVo.ok(result);
    }
}
