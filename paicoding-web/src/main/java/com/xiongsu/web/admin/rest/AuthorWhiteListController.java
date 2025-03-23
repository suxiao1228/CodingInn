package com.xiongsu.web.admin.rest;

import com.xiongsu.api.vo.ResVo;
import com.xiongsu.api.vo.user.dto.BaseUserInfoDTO;
import com.xiongsu.core.permission.Permission;
import com.xiongsu.core.permission.UserRole;
import com.xiongsu.service.user.service.AuthorWhiteListService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 作者白名单服务
 */
@RestController
@Tag(name = "发布文章作者白名单管理控制器", description = "作者白名单")
@Permission(role = UserRole.ADMIN)
@RequestMapping(path = {"api/admin/author/whitelist"})
public class AuthorWhiteListController {
    @Resource
    private AuthorWhiteListService authorWhiteListService;

    @GetMapping(path = "get")
    @Operation(summary = "白名单列表", description = "返回作者白名单列表")
    public ResVo<List<BaseUserInfoDTO>> whiteList() {
        return ResVo.ok(authorWhiteListService.queryAllArticleWhiteListAuthors());
    }

    /**
     * 将指定的authorId(作者Id)添加到白名单中
     * @param authorId
     * @return
     */
    @GetMapping(path = "add")
    @Operation(summary = "添加白名单", description = "将指定作者加入作者白名单列表")
    //name = "authorId"：参数名称（必须和 @RequestParam("authorId") 一致）。
    //description = "传入需要添加白名单的作者UserId"：参数的说明（这个参数是需要加入白名单的 UserId）。
    //required = true：表示这个参数是必须提供的。
    //allowEmptyValue = false：表示这个参数不能传空值。
    //example = "1"：提供一个示例值，表示这个参数的典型用法。
    @Parameter(name = "authorId", description = "传入需要添加白名单的作者UserId", required = true, allowEmptyValue = false, example = "1")
    public ResVo<Boolean> addAuthor(@RequestParam("authorId") Long authorId) {
        authorWhiteListService.addAuthor2ArticleWhitList(authorId);
        return ResVo.ok(true);
    }

    @GetMapping(path = "remove")
    @Operation(summary = "删除白名单", description = "将作者从白名单列表删除")
    @Parameter(name = "authorId", description = "传入需要删除白名单的作者UserId", required = true, allowEmptyValue = false, example = "1")
    public ResVo<Boolean> rmAuthor(@RequestParam("authorId") Long authorId) {
        authorWhiteListService.removeAuthorFromArticleWhiteList(authorId);
        return ResVo.ok(true);
    }
}

