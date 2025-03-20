package com.xiongsu.web.admin.rest;

import com.xiongsu.api.context.ReqInfoContext;
import com.xiongsu.api.vo.ResVo;
import com.xiongsu.api.vo.constants.StatusEnum;
import com.xiongsu.api.vo.user.dto.BaseUserInfoDTO;
import com.xiongsu.core.permission.Permission;
import com.xiongsu.core.permission.UserRole;
import com.xiongsu.core.util.SessionUtil;
import com.xiongsu.service.user.service.AuthorWhiteListService;
import com.xiongsu.service.user.service.LoginService;
import com.xiongsu.service.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Priority;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sound.sampled.Port;
import java.util.Optional;

/**
 * 文章后台
 */
@Slf4j
@RestController
@Tag(name = "后台登录登出管理控制器", description = "后台登陆")
@RequestMapping("/admin")//统一前缀，方便管理
public class AdminLoginController {

    @Resource
    private UserService userService;

    @Resource
    private LoginService loginOutService;

    @Resource
    private AuthorWhiteListService authorWhiteListService;

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }

    /**
     * 后台用户名 & 密码的方式登录
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(path = "login")
    public ResVo<BaseUserInfoDTO> login(@RequestBody LoginRequest request,
                                        HttpServletResponse response) {
        String username = request.getUsername();
        String pwd = request.getPassword();
        String session = loginOutService.loginByUserPwd(username, pwd);
        if (StringUtils.isNotBlank(session)) {
            //cookie中写入用户登录信息
            response.addCookie(SessionUtil.newCookie(LoginService.SESSION_KEY, session));
            return ResVo.ok(userService.queryBasicUserInfo(ReqInfoContext.getReqInfo().getUserId()));
        } else {
            return ResVo.fail(StatusEnum.LOGIN_FAILED_MIXED, "登陆失败，请重试");
        }
    }

    /**
     * 判断是否有登录
     * @return
     */
    @RequestMapping(path = "isLogined")
    public ResVo<Boolean> isLogined() {
        return ResVo.ok(ReqInfoContext.getReqInfo().getUserId() != null);
    }

    @Permission(role = UserRole.LOGIN)
    @GetMapping("logout")
    public ResVo<Boolean> logOut (HttpServletResponse response) {
        Optional.ofNullable(ReqInfoContext.getReqInfo()).ifPresent(s -> loginOutService.logout(s.getSession()));
        // 为什么不后端实现重定向？ 重定向交给前端执行，避免由于前后端分离，本地开发时端口不一致导致的问题
        // response.sendRedirect("/");

        // 移除cookie
        response.addCookie(SessionUtil.delCookie(LoginService.SESSION_KEY));
        return ResVo.ok(true);
    }
}
