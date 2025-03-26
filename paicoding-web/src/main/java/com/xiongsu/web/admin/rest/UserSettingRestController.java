package com.xiongsu.web.admin.rest;

import com.xiongsu.api.context.ReqInfoContext;
import com.xiongsu.api.vo.ResVo;
import com.xiongsu.api.vo.user.dto.BaseUserInfoDTO;
import com.xiongsu.api.vo.user.dto.SimpleUserInfoDTO;
import com.xiongsu.core.permission.Permission;
import com.xiongsu.core.permission.UserRole;
import com.xiongsu.service.user.service.UserService;
import com.xiongsu.web.controller.search.vo.SearchUserVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户权限管理后台
 */
@RestController
@Permission(role = UserRole.ADMIN)
@Tag(name = "用户管理控制器", description = "用户管理")
@RequestMapping(path = {"api/admin/user/", "admin/user/"})
public class UserSettingRestController {

    @Resource
    private UserService userService;

    @Operation(summary = "用户搜索")
    @GetMapping(path = "query")
    public ResVo<SearchUserVo> queryUserList(@RequestParam(name = "key", required = false) String key) {
        List<SimpleUserInfoDTO> list = userService.searchUser(key);
        SearchUserVo vo = new SearchUserVo();
        vo.setKey(key);
        vo.setItems(list);
        return ResVo.ok(vo);
    }

    @Permission(role = UserRole.LOGIN)
    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("info")
    public ResVo<BaseUserInfoDTO> info() {
        BaseUserInfoDTO user = ReqInfoContext.getReqInfo().getUser();
        return ResVo.ok(user);
    }
}
