package com.xiongsu.web.controller.user.rest;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xiongsu.api.context.ReqInfoContext;
import com.xiongsu.api.vo.ResVo;
import com.xiongsu.api.vo.article.dto.ArticleDTO;
import com.xiongsu.api.vo.constants.StatusEnum;
import com.xiongsu.api.vo.user.UserInfoSaveReq;
import com.xiongsu.api.vo.user.UserRelationReq;
import com.xiongsu.api.vo.user.dto.UserStatisticInfoDTO;
import com.xiongsu.core.permission.Permission;
import com.xiongsu.core.permission.UserRole;
import com.xiongsu.service.article.service.ArticleReadService;
import com.xiongsu.service.user.cache.UserInfoCacheManager;
import com.xiongsu.service.user.service.relation.UserRelationServiceImpl;
import com.xiongsu.service.user.service.user.UserServiceImpl;
import com.xiongsu.web.controller.user.vo.UserHomeInfoVo;
import com.xiongsu.web.global.vo.ResultVo;
import jakarta.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping(path = "user/api")
public class UserRestController {

    @Resource
    private UserServiceImpl userService;

    @Resource
    private UserRelationServiceImpl userRelationService;

    @Resource
    private ArticleReadService articleReadService;

    @Resource
    private UserInfoCacheManager userInfoCacheManager;

    private static final List<String> homeSelectTags = Arrays.asList("article", "read", "follow", "collection");
    private static final List<String> followSelectTags = Arrays.asList("follow", "fans");

    /**
     * 保存用户关系
     */
    @Permission(role = UserRole.LOGIN)//注解会自动获取 当前登录用户 ID，并在 saveUserRelation 里使用它
    @PostMapping(path = "saveUserRelation")
    public ResVo<Boolean> saveUserRelation(@RequestBody UserRelationReq req) {//followUserId永远都是当前的用户
        userRelationService.saveUserRelation(req);
        return ResVo.ok(true);
    }

    /**
     * 保存用户详情
     */
    @Permission(role = UserRole.LOGIN)
    @PostMapping(path = "saveUserInfo")
    @Transactional(rollbackFor = Exception.class)//保证事务的原子性
    public ResVo<Boolean> saveUserInfo(@RequestBody UserInfoSaveReq req) {
        //!Objects.equals(req.getUserId(), ReqInfoContext.getReqInfo().getUserId())判断当前登录的用户id和你输入的用户id是否相同
        if (req.getUserId() == null || !Objects.equals(req.getUserId(), ReqInfoContext.getReqInfo().getUserId())) {
            // 不能修改其他用户的信息
            return ResVo.fail(StatusEnum.FORBID_ERROR_MIXED, "无权修改");
        }
        userInfoCacheManager.delUserInfo(req.getUserId());
        userService.saveUserInfo(req);
        return ResVo.ok(true);
    }

    /**
     * 获取用户主页信息，通常只有作者本人才能进入这个页面
     */
    @Permission(role = UserRole.LOGIN)
    @GetMapping(path = "home")
    public ResultVo<UserHomeInfoVo> getUserHome(@RequestParam(name = "userId") Long userId) {
        UserHomeInfoVo vo = new UserHomeInfoVo();
        UserStatisticInfoDTO userInfo = userService.queryUserInfoWithStatistic(userId);
        vo.setUserHome(userInfo);

        //SpringUtil.getBean(SeoInjectService.class).initUserSeo(vo);
        return ResultVo.ok(vo);
    }

    /**
     *获取用户主页信息的文章列表
     */
    @Permission(role = UserRole.LOGIN)
    @GetMapping(path = "articles")
    public ResultVo<IPage<ArticleDTO>> getUserArticles(@RequestParam(name = "userId") Long userId,
                                                       @RequestParam(name = "currentPage", required = false, defaultValue = "1") int currentPage,
                                                       @RequestParam(name = "pageSize", required = false, defaultValue = "10") int pageSize) {
        return ResultVo.ok(articleReadService.queryArticlesByUserIdPagination(userId, currentPage, pageSize));
    }
}
