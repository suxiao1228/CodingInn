package com.xiongsu.web.front.article.rest;


import com.xiongsu.api.context.ReqInfoContext;
import com.xiongsu.api.enums.DocumentTypeEnum;
import com.xiongsu.api.enums.OperateTypeEnum;
import com.xiongsu.api.vo.*;
import com.xiongsu.api.vo.article.ArticlePostReq;
import com.xiongsu.api.vo.article.ContentPostReq;
import com.xiongsu.api.vo.article.dto.ArticleDTO;
import com.xiongsu.api.vo.article.dto.CategoryDTO;
import com.xiongsu.api.vo.article.dto.TagDTO;
import com.xiongsu.api.vo.constants.StatusEnum;
import com.xiongsu.api.vo.user.dto.BaseUserInfoDTO;
import com.xiongsu.core.mdc.MdcDot;
import com.xiongsu.core.permission.Permission;
import com.xiongsu.core.permission.UserRole;
import com.xiongsu.core.util.MarkdownConverter;
import com.xiongsu.service.article.repository.entity.ArticleDO;
import com.xiongsu.service.article.service.*;
import com.xiongsu.web.front.article.vo.ArticleDetailVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

@Slf4j
@RequestMapping(path="article/api")
@RestController
public class ArticleRestController {
    @Autowired
    private ArticleReadService articleReadService;
    @Autowired
    private UserFootService userFootService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private TagService tagService;
    @Autowired
    private ArticleReadService articleService;
    @Autowired
    private ArticleWriteService articleWriteService;

    @Autowired
    private TemplateEngineHelper templateEngineHelper;

    @Autowired
    private ArticleRecommendService articleRecommendService;

    @Autowired
    private UserService userService;

    /**
     * 文章详情页
     * - 参数解析知识点
     * - fixme * [1.Get请求参数解析姿势汇总 | 一灰灰Learning](https://hhui.top/spring-web/01.request/01.190824-springboot%E7%B3%BB%E5%88%97%E6%95%99%E7%A8%8Bweb%E7%AF%87%E4%B9%8Bget%E8%AF%B7%E6%B1%82%E5%8F%82%E6%95%B0%E8%A7%A3%E6%9E%90%E5%A7%BF%E5%8A%BF%E6%B1%87%E6%80%BB/)
     *
     * @param articleId
     * @return
     */
    @GetMapping("/data/detail/{articleId}")
    public ResVo<ArticleDetailVo> detail(@PathVariable(name = "articleId") Long articleId) throws IOException {
        ArticleDetailVo vo = new ArticleDetailVo();
        // 文章相关信息
        ArticleDTO articleDTO = articleService.queryFullArticleInfo(articleId, ReqInfoContext.getReqInfo().getUserId());
        // 返回给前端页面时，转换为html格式
        articleDTO.setContent(MarkdownConverter.markdownToHtml(articleDTO.getContent()));
        vo.setArticle(articleDTO);

        // 作者信息
        BaseUserInfoDTO user = userService.queryBasicUserInfo(articleDTO.getAuthor());
        articleDTO.setAuthorName(user.getUserName());
        articleDTO.setAuthorAvatar(user.getPhoto());
        return ResVo.ok(vo);
    }

    /**
     * 文章的关联推荐
     *
     * @param articleId
     * @param page
     * @param size
     * @return
     */
    @RequestMapping(path = "recommend")
    @MdcDot(bizCode = "#articleId")
    public ResVo<NextPageHtmlVo> recommend(@RequestParam(value = "articleId") Long articleId,
                                           @RequestParam(name = "page") Long page,
                                           @RequestParam(name = "size", required = false) Long size) {
        size = Optional.ofNullable(size).orElse(PageParam.DEFAULT_PAGE_SIZE);
        size = Math.min(size, PageParam.DEFAULT_PAGE_SIZE);
        PageListVo<ArticleDTO> articles = articleRecommendService.relatedRecommend(articleId, PageParam.newPageInstance(page, size));
        String html = templateEngineHelper.renderToVo("views/article-detail/article/list", "articles", articles);
        return ResVo.ok(new NextPageHtmlVo(html, articles.getHasMore()));
    }

    /**
     * 查询所有的标签
     *
     * @return
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
    @GetMapping(path = "tag/list")
    public ResVo<PageVo<TagDTO>> queryTags(@RequestParam(name = "key", required = false) String key,
                                           @RequestParam(name = "pageNumber", required = false, defaultValue = "1") Integer pageNumber,
                                           @RequestParam(name = "pageSize", required = false, defaultValue = "10") Integer pageSize) {
        PageVo<TagDTO> tagDTOPageVo = tagService.queryTags(key, PageParam.newPageInstance(pageNumber, pageSize));
        return ResVo.ok(tagDTOPageVo);
    }

    /**
     * 获取所有的分类
     */
    @GetMapping(path = "category/list")
    public ResVo<List<CategoryDTO>> getCategoryList(@RequestParam(name = "categoryId", required = false) Long categoryId,
                                                    @RequestParam(name = "ignoreNoArticles", required = false) Boolean ignoreNoArticles) {
        List<CategoryDTO> list = categoryService.loadAllCategories();
        if (Objects.equals(Boolean.TRUE, ignoreNoArticles)) {
            // 查询所有分类的对应的文章数
            Map<Long, Long> articleCnt = articleService.queryArticleCountsByCategory();
            // 过滤掉文章数为0的分类
            list.removeIf(c -> articleCnt.getOrDefault(c.getCategoryId(), 0L) <= 0L);
        }
        list.forEach(c -> c.setSelected(c.getCategoryId().equals(categoryId)));
        return ResVo.ok(list);
    }

    /**
     * 收藏，点赞等相关操作
     */
    @Permission(role = UserRole.LOGIN)
    @GetMapping(path = "favor")
    @MdcDot(bizCode = "#articleId")
    public ResVo<Boolean> favor(@RequestParam(name = "articleId") Long articleId,
                                @RequestParam(name = "type") Integer type) throws IOException, TimeoutException {
        OperateTypeEnum operate = OperateTypeEnum.fromCode(type);
        if (operate == OperateTypeEnum.EMPTY) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, type + "非法");
        }

        //要求文章必须存在
        ArticleDO article = articleReadService.queryBasicArticle(articleId);
        if (article == null) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS_MIXED, "文章不存在!");
        }
        // 更新用户与文章的点赞/收藏状态
        userFootService.favorArticleComment(DocumentTypeEnum.ARTICLE, articleId, article.getUserId(),
                ReqInfoContext.getReqInfo().getUserId(),
                operate);
        return ResVo.ok(true);
    }
    /**
     * 发布文章，完成后跳转到详情页
     * - 这里有一个重定向的知识点
     * - fixme 博文：* [5.请求重定向 | 一灰灰Learning](https://hhui.top/spring-web/02.response/05.190929-springboot%E7%B3%BB%E5%88%97%E6%95%99%E7%A8%8Bweb%E7%AF%87%E4%B9%8B%E9%87%8D%E5%AE%9A%E5%90%91/)
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
    }


    /**
     * 文章删除
     *
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
