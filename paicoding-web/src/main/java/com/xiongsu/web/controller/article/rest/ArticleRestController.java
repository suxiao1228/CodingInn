package com.xiongsu.web.controller.article.rest;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xiongsu.api.context.ReqInfoContext;
import com.xiongsu.api.enums.DocumentTypeEnum;
import com.xiongsu.api.enums.NotifyTypeEnum;
import com.xiongsu.api.enums.OperateTypeEnum;
import com.xiongsu.api.event.MessageQueueEvent;
import com.xiongsu.api.vo.PageParam;
import com.xiongsu.api.vo.PageVo;
import com.xiongsu.api.vo.ResVo;
import com.xiongsu.api.vo.article.ArticlePostReq;
import com.xiongsu.api.vo.article.ContentPostReq;
import com.xiongsu.api.vo.article.dto.ArticleDTO;
import com.xiongsu.api.vo.article.dto.ArticleOtherDTO;
import com.xiongsu.api.vo.article.dto.CategoryDTO;
import com.xiongsu.api.vo.article.dto.TagDTO;
import com.xiongsu.api.vo.article.response.CategoryArticlesResponseDTO;
import com.xiongsu.api.vo.comment.dto.TopCommentDTO;
import com.xiongsu.api.vo.constants.StatusEnum;
import com.xiongsu.api.vo.notify.NotifyMsgEvent;
import com.xiongsu.api.vo.recommend.SideBarDTO;
import com.xiongsu.api.vo.user.dto.UserStatisticInfoDTO;
import com.xiongsu.core.common.CommonConstants;
import com.xiongsu.core.mdc.MdcDot;
import com.xiongsu.core.permission.Permission;
import com.xiongsu.core.permission.UserRole;
import com.xiongsu.core.util.SpringUtil;
import com.xiongsu.service.article.repository.entity.ArticleDO;
import com.xiongsu.service.article.repository.entity.ColumnArticleDO;
import com.xiongsu.service.article.service.*;
import com.xiongsu.service.comment.service.CommentReadService;
import com.xiongsu.service.notify.service.RabbitmqService;
import com.xiongsu.service.sidebar.service.SidebarService;
import com.xiongsu.service.user.repository.entity.UserFootDO;
import com.xiongsu.service.user.service.UserFootService;
import com.xiongsu.service.user.service.UserService;
import com.xiongsu.web.controller.article.vo.ArticleDetailVo;
import com.xiongsu.web.controller.article.vo.ArticleEditVo;
import com.xiongsu.web.controller.home.helper.IndexRecommendHelper;
import com.xiongsu.web.global.vo.ResultVo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

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

//    @Resource
//    private ArticleRecommendService articleRecommendService;

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
    IndexRecommendHelper indexRecommendHelper;

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
    @GetMapping("/article/category")
    public ResultVo<CategoryArticlesResponseDTO> getArticlesByCategory(@RequestParam(name = "category", required = false) String category,
                                                                       @RequestParam(name = "currentPage", required = false, defaultValue = "1") int currentPage,
                                                                       @RequestParam(name = "pageSize", required = false, defaultValue = "10") int pageSize) {

        // 搜索对应的文章
        IPage<ArticleDTO> articles = articleService.queryArticlesByCategoryPagination(currentPage, pageSize, category);

        List<CategoryDTO> categories = categoryService.loadAllCategories();
        // 查询所有分类的对应的文章数
        Map<Long, Long> articleCnt = articleService.queryArticleCountsByCategory();
        // 过滤掉文章数为0的分类
        categories.removeIf(c -> articleCnt.getOrDefault(c.getCategoryId(), 0L) <= 0L);

        CategoryDTO selectedCategory = categories.stream().filter(c -> c.getCategory().equals(category)).findFirst().orElse(null);
        selectedCategory = selectedCategory == null ? CategoryDTO.DEFAULT_CATEGORY : selectedCategory;
        List<ArticleDTO> topArticles = indexRecommendHelper.topArticleList(selectedCategory);

        CategoryArticlesResponseDTO responseDTO = new CategoryArticlesResponseDTO(articles, categories, topArticles);
        return ResultVo.ok(responseDTO);
    }

    /**
     * 获取指定分类下的文章信息
     */
    @GetMapping("/articles/tag")
    public ResultVo<IPage<ArticleDTO>> getArticlesByTag(@RequestParam(name = "tagId", required = false) Long tagId,
                                                        @RequestParam(name = "currentPage", required = false, defaultValue = "1") int currentPage,
                                                        @RequestParam(name = "pageSize", required = false, defaultValue = "10") int pageSize) {
        IPage<ArticleDTO> articles = articleService.queryArticlesByTagPagination(currentPage, pageSize, tagId);
        return ResultVo.ok(articles);
    }


    /**
     * 收藏，点赞等相关操作
     * @param articleId
     * @param type
     * @return
     */
    @Permission(role = UserRole.LOGIN)
    @GetMapping(path = "favor")
    @MdcDot(bizCode = "#articleId")
    public ResVo<Boolean> favor(@RequestParam(name = "articleId") Long articleId,
                                @RequestParam(name = "type") Integer type ) {
        if(log.isDebugEnabled()) {
            log.debug("开始点赞: {}", type);
        }
        OperateTypeEnum operate = OperateTypeEnum.fromCode(type);
        if (operate == OperateTypeEnum.EMPTY) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, type + "非法");
        }

        //要求文章必须存在
        ArticleDO article = articleReadService.queryBasicArticle(articleId);
        if (article == null) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "文章不存在!");
        }

        UserFootDO foot = userFootService.saveOrUpdateUserFoot(DocumentTypeEnum.ARTICLE, articleId, article.getUserId(),
                ReqInfoContext.getReqInfo().getUserId(),
                operate);
        //点赞，收藏消息
        NotifyTypeEnum notifyType = OperateTypeEnum.getNotifyType(operate);

        //点赞消息走 Rabbitmq，其他走Java内置消息机制
        if ((notifyType.equals(NotifyTypeEnum.PRAISE) || notifyType.equals(NotifyTypeEnum.CANCEL_PRAISE)) && rabbitmqService.enabled()) {
            rabbitmqService.publishDirectMsg(new MessageQueueEvent<>(notifyType, foot), CommonConstants.MESSAGE_QUEUE_KEY_NOTIFY);
        } else {
            Optional.ofNullable(notifyType).ifPresent(notify -> SpringUtil.publishEvent(new NotifyMsgEvent<>(this, notify, foot)));
        }

        if (log.isDebugEnabled()) {
            log.info("点赞结束: {}", type);
        }
        return ResVo.ok(true);
    }

    /**
     * 发布文章，完成后跳转到详情页
     *
     * @return
     */
    @Permission(role = UserRole.LOGIN)
    @PostMapping(path = "post")
    @MdcDot(bizCode = "#req.articleId")
    public ResVo<Long> post(@RequestBody ArticlePostReq req, HttpServletResponse response) throws IOException {
        Long id = articleWriteService.saveArticle(req, ReqInfoContext.getReqInfo().getUserId());
        // 如果使用后端重定向，可以使用下面两种策略
//        return "redirect:/article/detail/" + id;
//        response.sendRedirect("/article/detail/" + id);
        // 这里采用前端重定向策略
        return ResVo.ok(id);
        //记得往postman里面加一下
    }

    /**
     * 更新文章
     * @param articleId
     * @return
     */
    @Permission(role = UserRole.LOGIN)
    @GetMapping(path = "update/{articleId}")
    public ResultVo<ArticleEditVo> update(@PathVariable(name = "articleId") Long articleId) {
        ArticleEditVo vo = new ArticleEditVo();
        if (articleId != null) {
            ArticleDTO article = articleService.queryDetailArticleInfo(articleId);
            vo.setArticle(article);
            if (!Objects.equals(article.getAuthor(), ReqInfoContext.getReqInfo().getUserId())) {
                // 没有权限
                return ResultVo.fail(StatusEnum.NO_PERMISSION, "没有权限");
            }
            List<CategoryDTO> categoryList = categoryService.loadAllCategories();
            vo.setCategories(categoryList);
            vo.setTags(article.getTags());
        } else {
            List<CategoryDTO> categoryList = categoryService.loadAllCategories();
            vo.setCategories(categoryList);
            vo.setTags(Collections.emptyList());
        }
        return ResultVo.ok(vo);
    }

    /**
     * 文章删除
     * @param articleId
     * @return
     */
    @Permission(role = UserRole.LOGIN)
    @RequestMapping(path = "delete")
    @MdcDot(bizCode = "#articleId")
    public ResVo<Boolean> delete(@RequestParam(value = "articleId") Long articleId) {
        articleWriteService.deleteArticle(articleId, ReqInfoContext.getReqInfo().getUserId());
        return ResVo.ok(true);
    }
}
