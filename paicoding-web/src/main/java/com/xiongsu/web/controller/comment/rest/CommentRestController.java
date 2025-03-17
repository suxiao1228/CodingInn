package com.xiongsu.web.controller.comment.rest;

import com.baomidou.mybatisplus.core.toolkit.sql.StringEscape;
import com.xiongsu.api.context.ReqInfoContext;
import com.xiongsu.api.vo.PageParam;
import com.xiongsu.api.vo.ResVo;
import com.xiongsu.api.vo.comment.CommentSaveReq;
import com.xiongsu.api.vo.comment.dto.TopCommentDTO;
import com.xiongsu.api.vo.constants.StatusEnum;
import com.xiongsu.core.permission.Permission;
import com.xiongsu.core.permission.UserRole;
import com.xiongsu.core.util.NumUtil;
import com.xiongsu.service.article.conveter.ArticleConverter;
import com.xiongsu.service.article.repository.entity.ArticleDO;
import com.xiongsu.service.article.repository.entity.ArticleDetailDO;
import com.xiongsu.service.article.service.ArticleReadService;
import com.xiongsu.service.comment.service.CommentReadService;
import com.xiongsu.service.comment.service.CommentWriteService;
import com.xiongsu.service.user.service.UserFootService;
import com.xiongsu.web.controller.article.vo.ArticleDetailVo;
import jakarta.annotation.Resource;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 评论
 */

@RestController
@RequestMapping(path = "comment/api")
public class CommentRestController {
    @Resource
    private ArticleReadService articleReadService;

    @Resource
    private CommentReadService commentReadService;

    @Resource
    private CommentWriteService commentWriteService;

    @Resource
    private UserFootService userFootService;

    /**
     * 评论列表页
     */
    @ResponseBody
    @RequestMapping(path = "list")
    public ResVo<List<TopCommentDTO>> list(Long articleId, Long pageNum, Long pageSize) {
        if (NumUtil.nullOrZero(articleId)) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "文章id为空");
        }
        pageNum = Optional.ofNullable(pageNum).orElse(PageParam.DEFAULT_PAGE_NUM);
        pageSize = Optional.ofNullable(pageSize).orElse(PageParam.DEFAULT_PAGE_SIZE);
        List<TopCommentDTO> result = commentReadService.getArticleComments(articleId, PageParam.newPageInstance(pageNum, pageSize));
        return ResVo.ok(result);
    }

    /**
     * 保存评论
     * @param req
     * @return
     */
    @Permission(role = UserRole.LOGIN)
    @PostMapping(path = "/save")
    @ResponseBody
    public ResVo<ArticleDetailVo> saveComment(@RequestBody CommentSaveReq req) {
        if (req.getArticleId() == null) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "文章id为空");
        }
        ArticleDO article = articleReadService.queryBasicArticle(req.getArticleId());
        if (article == null) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "文章不存在");
        }

        //保存评论
        req.setUserId(ReqInfoContext.getReqInfo().getUserId());
        req.setCommentContent(StringEscapeUtils.escapeHtml3(req.getCommentContent()));
        commentWriteService.saveComment(req);

        //返回新的评论信息，用于实时更新详情页的评论列表
        ArticleDetailVo vo = new ArticleDetailVo();
        vo.setArticle(ArticleConverter.toDto(article));

        // 评论信息
        List<TopCommentDTO> comments = commentReadService.getArticleComments(req.getArticleId(), PageParam.newPageInstance());
        vo.setComments(comments);

        //热门评论
        TopCommentDTO hotComment = commentReadService.queryHotComment(req.getArticleId());
        vo.setHotComment(hotComment);
        return ResVo.ok(vo);
    }

    /**
     * 删除评论
     * @param commentId
     * @return
     */
    @Permission(role = UserRole.LOGIN)
    @RequestMapping(path = "delete")
    public ResVo<Boolean> delete(Long commentId) {
        commentWriteService.deleteComment(commentId, ReqInfoContext.getReqInfo().getUserId());
        return ResVo.ok(true);
    }

}
