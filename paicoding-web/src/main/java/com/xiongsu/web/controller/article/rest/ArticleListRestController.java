package com.xiongsu.web.controller.article.rest;

import com.xiongsu.api.vo.PageListVo;
import com.xiongsu.api.vo.PageParam;
import com.xiongsu.api.vo.ResVo;
import com.xiongsu.api.vo.article.dto.ArticleDTO;
import com.xiongsu.service.article.service.ArticleReadService;
import com.xiongsu.web.global.BaseViewController;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping(path = "article/api/list")
@RestController
public class ArticleListRestController extends BaseViewController {
    @Resource
    private ArticleReadService articleService;

    /**
     * 分类下的文章列表
     *
     * @param categoryId 类目id
     * @param page 请求页
     * @param size 分页数
     * @return 文章列表
     */
    @GetMapping(path = "data/category/{category}")
    public ResVo<PageListVo<ArticleDTO>> categoryDataList(@PathVariable("category") Long categoryId,
                                                          @RequestParam(name = "page") Long page,
                                                          @RequestParam(name = "size", required = false) Long size) {
        PageParam pageParam = buildPageParam(page, size);
        PageListVo<ArticleDTO> list = articleService.queryArticlesByCategory(categoryId, pageParam);
        return ResVo.ok(list);
    }

}
