package com.xiongsu.web.hook.interceptor;

import com.xiongsu.api.context.ReqInfoContext;
import com.xiongsu.core.util.SpringUtil;
import com.xiongsu.service.sitemap.service.impl.SitemapServiceImpl;
import com.xiongsu.service.statistics.service.statistic.UserStatisticService;
import com.xiongsu.service.statistics.service.statistic.UserStatisticServiceProperties;
import jakarta.annotation.Resource;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jodd.net.HttpMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.AsyncHandlerInterceptor;

import java.util.UUID;

@Slf4j
@Configuration
@EnableConfigurationProperties(UserStatisticServiceProperties.class)//启用 UserStatisticServiceProperties 这个配置属性类，并让它成为 Spring 容器的 Bean。
public class UserStatisticsInterceptor {
    @Resource
    private UserStatisticService userStatisticService;

    private static final String SESSION_COOKIE_NAME = "SESSION_ID";

    //两种不同的在线用户统计方式
    @Bean
    public AsyncHandlerInterceptor onlineUserStatisticInterceptor(UserStatisticServiceProperties userStatisticServiceProperties) {
        if(UserStatisticServiceProperties.UserStatisticServiceType.CAFFEINE.equals(userStatisticServiceProperties.getType())) {
            return new OnlineUserByCookieInterceptor();
        }else{
            return new OnlineUserBySessionInterceptor();
        }
    }

    private class OnlineUserByCookieInterceptor implements AsyncHandlerInterceptor {
        public OnlineUserByCookieInterceptor() {
            log.info("【OnlineUserByCookieInterceptor】 init");
        }

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            if (HttpMethod.OPTIONS.toString().equals(request.getMethod())) {
                return true;
            }
            //更新pv,uv计数
            SpringUtil.getBean(SitemapServiceImpl.class).saveVisitInfo(ReqInfoContext.getReqInfo().getClientIp(), ReqInfoContext.getReqInfo().getPath());

            //检查请求中是否包含 SESSION_ID Cookie
            String sessionId = getSessionIdFromCookies(request);

            if (sessionId == null) {
                // 如果没有sessionId, 则生成新的 sessionId
                sessionId = UUID.randomUUID().toString();

                // 将sessionId 存入缓存， 标记用户为在线
                userStatisticService.incrOnlineUserCnt(1);

                //将sessionId 添加到Cookie返回给浏览器
                Cookie sessionCookie = new Cookie(SESSION_COOKIE_NAME, sessionId);
                //防止脚本访问
                sessionCookie.setHttpOnly(true);
                sessionCookie.setPath("/");
                // 30分钟有效期
                sessionCookie.setMaxAge(30 * 60);
                response.addCookie(sessionCookie);
            } else {
                // 如果携带了 sessionId，检查是否存在于缓存中
                if (!userStatisticService.isOnline(sessionId)) {
                    // 如果缓存中不存在该 sessionId，则标记用户为在线并存入缓存
                    userStatisticService.invalidateSession(sessionId);
                }
                userStatisticService.updateSessionExpireTime(sessionId);
            }
            //继续处理请求
            return true;
        }

        // 从请求的 Cookie 中获取 sessionId
        private String getSessionIdFromCookies(HttpServletRequest request) {
            if (request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    if (SESSION_COOKIE_NAME.equals(cookie.getName())) {
                        return cookie.getValue();
                    }
                }
            }
            return null;
        }
    }

    private class OnlineUserBySessionInterceptor implements AsyncHandlerInterceptor {
        public OnlineUserBySessionInterceptor() {
            log.info("【OnlineUserBySessionInterceptor】 init");
        }

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            if(org.springframework.http.HttpMethod.OPTIONS.toString().equals(request.getMethod())){
                return true;
            }
            // 更新uv/pv计数
            SpringUtil.getBean(SitemapServiceImpl.class).saveVisitInfo(ReqInfoContext.getReqInfo().getClientIp(), ReqInfoContext.getReqInfo().getPath());

            HttpSession session = request.getSession(true);
            session.setMaxInactiveInterval(30);


            // 检查请求中是否包含 SESSION_ID Cookie
//        String sessionId = getSessionIdFromCookies(request);

            if (session.isNew()) {
                // 如果没有 sessionId，则生成新的 sessionId
                String sessionId = session.getId();

                // 将 sessionId 存入缓存，标记用户为在线
//            userStatisticService.incrOnlineUserCnt(1);

                // 将 sessionId 添加到 Cookie 返回给浏览器
                Cookie sessionCookie = new Cookie(SESSION_COOKIE_NAME, sessionId);
                // 防止脚本访问
                sessionCookie.setHttpOnly(true);
                sessionCookie.setPath("/");
                // 30分钟有效期
                sessionCookie.setMaxAge(30);
                response.addCookie(sessionCookie);
            }
            // 继续处理请求
            return true;

        }

        // 从请求的 Cookie 中获取 sessionId
        private String getSessionIdFromCookies(HttpServletRequest request) {
            if (request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    if (SESSION_COOKIE_NAME.equals(cookie.getName())) {
                        return cookie.getValue();
                    }
                }
            }
            return null;
        }
    }
}
