package com.xiongsu.web.controller.login.pwd;


import com.xiongsu.api.context.ReqInfoContext;
import com.xiongsu.api.vo.ResVo;
import com.xiongsu.api.vo.constants.StatusEnum;
import com.xiongsu.api.vo.login.UserNamePasswordReq;
import com.xiongsu.core.permission.Permission;
import com.xiongsu.core.permission.UserRole;
import com.xiongsu.core.util.SessionUtil;
import com.xiongsu.service.user.service.LoginService;
import com.xiongsu.web.controller.home.vo.LoginSuccessVo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping
public class LoginRestController {

    @Resource
    private LoginService loginService;

    /**
     * 用户名和密码登录
     */
    @PostMapping("new/login/username")
    public ResVo<LoginSuccessVo> loginByPassword(@RequestBody UserNamePasswordReq req,
                                         HttpServletResponse response) {
        String session = loginService.loginByUserPwd(req.getUsername(), req.getPassword());
        if(StringUtils.isNotBlank(session)) {
            //cookie中写入用户登录信息，用于身份识别
            Cookie cookie = SessionUtil.newCookie(loginService.SESSION_KEY, session);
            response.addCookie(cookie);
            return ResVo.ok(new LoginSuccessVo(cookie.getValue()));
        } else{
            return ResVo.fail(StatusEnum.LOGIN_FAILED_MIXED, "用户名和密码登录异常，请稍后重试");
        }
    }

    @Permission(role = UserRole.LOGIN)
    @RequestMapping("/logout")
    public ResVo<Boolean> logout(HttpServletRequest request, HttpServletResponse response) {
        //释放会话
        Optional.ofNullable(ReqInfoContext.getReqInfo()).ifPresent(s -> loginService.logout(s.getSession()));
        // 移除cookie
        response.addCookie(SessionUtil.delCookie(LoginService.SESSION_KEY));

        return ResVo.ok(true);
    }

}
