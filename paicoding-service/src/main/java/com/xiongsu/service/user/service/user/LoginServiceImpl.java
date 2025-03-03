package com.xiongsu.service.user.service.user;

import com.xiongsu.api.context.ReqInfoContext;
import com.xiongsu.api.exception.ExceptionUtil;
import com.xiongsu.api.vo.constants.StatusEnum;
import com.xiongsu.api.vo.user.UserPwdLoginReq;
import com.xiongsu.service.user.repository.dao.UserDao;
import com.xiongsu.service.user.repository.entity.UserDO;
import com.xiongsu.service.user.service.LoginService;
import com.xiongsu.service.user.service.UserAiService;
import com.xiongsu.service.user.service.UserService;
import com.xiongsu.service.user.service.help.UserPwdEncoder;
import com.xiongsu.service.user.service.help.UserSessionHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 基于验证码，用户名密码的登录服务
 */

@Service
@Slf4j
public class LoginServiceImpl implements LoginService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserAiDao userAiDao;


    @Autowired
    private UserSessionHelper userSessionHelper;
    @Autowired
    private StarNumberHelper starNumberHelper;

    @Autowired
    private RegisterService registerService;

    @Autowired
    private UserPwdEncoder userPwdEncoder;

    @Autowired
    private UserService userService;

    @Autowired
    private UserAiService userAiService;

    @Override
    public Long autoRegisterWxUserInfo(String uuid) {
        return 0L;
    }

    /**
     * 登出
     */
    @Override
    public void logout(String session) {
        userSessionHelper.removeSession(session);
    }

    @Override
    public String loginByWx(Long userId) {
        return "";
    }

    /**
     * 用户名密码登录
     */
    public String loginByUserPwd(String username, String password) {
        UserDO user = userDao.getUserByUserName(username);
        if(user == null) {
            throw ExceptionUtil.of(StatusEnum.USER_NOT_EXISTS, "userName=" + username);
        }
        if (!userPwdEncoder.match(password, user.getPassword())) {
            throw ExceptionUtil.of(StatusEnum.USER_PWD_ERROR);
        }

        Long userId = user.getId();
        // 1. 为了兼容历史数据，对于首次登录成功的用户，初始化ai信息
        userAiService.initOrUpdateAiInfo(new UserPwdLoginReq().setUserId(userId).setUsername(username).setPassword(password));

        // 登录成功，返回对应的session
        ReqInfoContext.getReqInfo().setUserId(userId);
        return userSessionHelper.genToken(userId);
    }

    /**
     * 用户名密码方式登录，若用户不存在，则进行注册
     *
     * @param loginReq 登录信息
     * @return
     */
    @Override
    public String registerByUserPwd(UserPwdLoginReq loginReq) {
        // 1. 前置校验
      //  registerPreCheck(loginReq);

        // 2. 判断当前用户是否登录，若已经登录，则直接走绑定流程
        Long userId = ReqInfoContext.getReqInfo().getUserId();
        loginReq.setUserId(userId);
        if (userId != null) {
            // 2.1 如果用户已经登录，则走绑定用户信息流程
            userService.bindUserInfo(loginReq);
            return ReqInfoContext.getReqInfo().getSession();
        }


        // 3. 尝试使用用户名进行登录，若成功，则依然走绑定流程
        UserDO user = userDao.getUserByUserName(loginReq.getUsername());
        if (user != null) {
            if (!userPwdEncoder.match(loginReq.getPassword(), user.getPassword())) {
                // 3.1 用户名已经存在
                throw ExceptionUtil.of(StatusEnum.USER_LOGIN_NAME_REPEAT, loginReq.getUsername());
            }

            // 3.2 用户存在，尝试走绑定流程
            userId = user.getId();
            loginReq.setUserId(userId);
            userAiService.initOrUpdateAiInfo(loginReq);
        } else {
            //4. 走用户注册流程
            userId = registerService.registerByUserNameAndPassword(loginReq);
        }
        ReqInfoContext.getReqInfo().setUserId(userId);
        return userSessionHelper.genToken(userId);
    }
}
