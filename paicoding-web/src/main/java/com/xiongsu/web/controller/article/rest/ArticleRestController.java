package com.xiongsu.web.controller.article.rest;

import com.xiongsu.api.context.ReqInfoContext;
import com.xiongsu.api.vo.PageParam;
import com.xiongsu.api.vo.PageVo;
import com.xiongsu.api.vo.ResVo;
import com.xiongsu.api.vo.article.ContentPostReq;
import com.xiongsu.api.vo.article.dto.ArticleDTO;
import com.xiongsu.api.vo.article.dto.ArticleOtherDTO;
import com.xiongsu.api.vo.article.dto.CategoryDTO;
import com.xiongsu.api.vo.article.dto.TagDTO;
import com.xiongsu.api.vo.comment.dto.TopCommentDTO;
import com.xiongsu.api.vo.recommend.SideBarDTO;
import com.xiongsu.api.vo.user.dto.UserStatisticInfoDTO;
import com.xiongsu.service.article.repository.entity.ColumnArticleDO;
import com.xiongsu.service.article.service.ArticleReadService;
import com.xiongsu.service.article.service.CategoryService;
import com.xiongsu.service.article.service.ColumnService;
import com.xiongsu.service.article.service.TagService;
import com.xiongsu.service.comment.service.CommentReadService;
import com.xiongsu.service.sidebar.service.SidebarService;
import com.xiongsu.service.user.service.UserFootService;
import com.xiongsu.service.user.service.UserService;
import com.xiongsu.web.controller.article.vo.ArticleDetailVo;
import com.xiongsu.web.global.vo.ResultVo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 返回json格式数据
 *
 */
@Slf4j
@RequestMapping(path = "article/api")
@RestController
public class ArticleRestController {
    @Resource
    private ArticleReadService articleReadService;
    @Resource
    private UserFootService userFootService;
    @Resource
    private CategoryService categoryService;
    @Resource
    private TagService tagService;
    @Resource
    private ArticleReadService articleService;
    @Resource
    private ArticleWriteService articleWriteService;

    @Resource
    private ArticleRecommendService articleRecommendService;

    @Resource
    private RabbitmqService rabbitmqService;

    @Resource
    private UserService userService;

    @Resource
    private CommentReadService commentService;

    @Resource
    private SidebarService sidebarService;

    @Resource
    private ColumnService columnService;

    @Resource
    IndexRecommesndHelper indexRecommendHelper;

    /**
     * 文章详情页
     */
    @GetMapping("/data/detail/{articleId}")
    public ResultVo<ArticleDetailVo> detailOriginalMarkdown(@PathVariable(name = "articleId") Long articleId) throws IOException {
        //针对专栏文章，做一个重定向
        ColumnArticleDO columnArticle = columnService.getColumnArticleRelation(articleId);
        ArticleDetailVo vo = new ArticleDetailVo();

        if (columnArticle != null) {
            vo.setColumnId(columnArticle.getColumnId());
            vo.setSectionId(columnArticle.getSection());
            return ResultVo.ok(vo, true);
        }

        //文章相关信息
        ArticleDTO articleDTO = articleService.queryFullArticleInfo(articleId, ReqInfoContext.getReqInfo().getUserId());
        // 返回给前端页面时，转换为html格式
        articleDTO.setContent(articleDTO.getContent());
        vo.setArticle(articleDTO);

        //评论信息
        List<TopCommentDTO> comments = commentService.getArticleComments(articleId, PageParam.newPageInstance(1L, 10L));
        vo.setComments(comments);

        //热门评论
        TopCommentDTO hotComment = commentService.queryHotComment(articleId);
        vo.setHotComment(hotComment);

        //其他信息封装
        ArticleOtherDTO other = new ArticleOtherDTO();
        //作者信息
        UserStatisticInfoDTO user = userService.queryUserInfoWithStatistic(articleDTO.getAuthor());
        articleDTO.setAuthorName(user.getUserName());
        articleDTO.setAuthorAvatar(user.getPhoto());
        vo.setAuthor(user);

        vo.setOther(other);

        //详情页的侧边推荐信息
        List<SideBarDTO> sideBars = sidebarService.queryArticleDetailSidebarList(articleDTO.getAuthor(), articleDTO.getArticleId());
        vo.setSideBarItems(sideBars);
        return ResultVo.ok(vo);
    }

    /**
     * 查询所有的标签
     */
    @PostMapping(path = "generateSummary")
    public ResVo<String> generateSummary(@RequestBody ContentPostReq req) {
        return ResVo.ok(articleService.generateSummary(req.getContent()));
    }
    /**
     * 查询所有的标签
     *
     * @return
     */
    @GetMapping(path = "tag/list")//参数key,是tag（标签表）的名字，会根据key进行搜索
    public ResVo<PageVo<TagDTO>> queryTags(@RequestParam(name = "key", required = false) String key,
                                           @RequestParam(name = "pageNumber", required = false, defaultValue = "1") Integer pageNumber,
                                           @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize) {
        PageVo<TagDTO> tagDTOPageVo = tagService.queryTags(key, PageParam.newPageInstance(pageNumber, pageSize));
        return ResVo.ok(tagDTOPageVo);
    }

    /**
     * 查询所有的分类
     */
    @GetMapping(path = "category/list")
    public ResVo<CategoryDTO> getCategoryList(@RequestParam(name = "categoryId", required = false) Long categoryId,
                                              @RequestParam(name = "ignoreNoArticles", required = false) Boolean ignoreNoArticles) {
        List<CategoryDTO> list = categoryService.loadAllCategories();
        if (Objects.equals(Boolean.TRUE, ignoreNoArticles)) {
            // 查询所有分类的对应的文章数
            Map<Long, Long> articleCnt = articleService.queryArticleCountsByCategory();
            // 过滤掉文章数为0的分类
            list.removeIf(c -> articleCnt.getOrDefault(c.getCategoryId(), 0L) <= 0L);
        }
//        list.forEach(c -> c.setSelected(c.getCategoryId().equals(categoryId)));
        return ResVo.ok((CategoryDTO) list);
    }

    /**
     * 获取指定分类下的文章信息
     */
    @GetMapping("/article")

}
