package com.xiongsu.web.hook.interceptor;


import com.github.hui.quick.plugin.qrcode.util.json.JsonUtil;
import com.xiongsu.api.context.ReqInfoContext;
import com.xiongsu.api.vo.ResVo;
import com.xiongsu.api.vo.constants.StatusEnum;
import com.xiongsu.core.util.SpringUtil;
import com.xiongsu.web.global.GlobalInitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;

import java.security.Permission;


/**
 * 注入全局的配置信息：
 * 站点信息，基本信息，在这里注入
 */

@Slf4j
@Component
@Order(-1)
public class GlobalViewInterceptor implements AsyncHandlerInterceptor {

    @Autowired
    private GlobalInitService globalInitService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {if (handler instanceof HandlerMethod) {
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Permission permission = handlerMethod.getMethod().getAnnotation(Permission.class);
        if (permission == null) {
            permission = handlerMethod.getBeanType().getAnnotation(Permission.class);
        }

        if (permission == null || permission.role() == UserRole.ALL) {
            if (ReqInfoContext.getReqInfo() != null) {
                // 用户活跃度更新
                SpringUtil.getBean(UserActivityRankService.class).addActivityScore(ReqInfoContext.getReqInfo().getUserId(), new ActivityScoreBo().setPath(ReqInfoContext.getReqInfo().getPath()));
            }
            return true;
        }

        if (ReqInfoContext.getReqInfo() == null || ReqInfoContext.getReqInfo().getUserId() == null) {
            if (handlerMethod.getMethod().getAnnotation(ResponseBody.class) != null
                    || handlerMethod.getMethod().getDeclaringClass().getAnnotation(RestController.class) != null) {
                // 访问需要登录的rest接口
                response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
                response.getWriter().println(JsonUtil.toStr(ResVo.fail(StatusEnum.FORBID_NOTLOGIN)));
                response.getWriter().flush();
                return false;
            } else if (request.getRequestURI().startsWith("/api/admin/") || request.getRequestURI().startsWith("/admin/")) {
                response.sendRedirect("/admin");
            } else {
                // 访问需要登录的页面时，直接跳转到登录界面
                response.sendRedirect("/");
            }
            return false;
        }
        if (permission.role() == UserRole.ADMIN && !UserRole.ADMIN.name().equalsIgnoreCase(ReqInfoContext.getReqInfo().getUser().getRole())) {
            // 设置为无权限
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return false;
        }
    }
        return true;
    }
}
