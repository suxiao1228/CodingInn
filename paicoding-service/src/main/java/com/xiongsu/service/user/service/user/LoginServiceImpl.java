package com.xiongsu.service.user.service.user;

import com.xiongsu.api.context.ReqInfoContext;
import com.xiongsu.api.exception.ExceptionUtil;
import com.xiongsu.api.vo.constants.StatusEnum;
import com.xiongsu.api.vo.user.UserPwdLoginReq;
import com.xiongsu.service.user.repository.entity.UserDO;
import com.xiongsu.service.user.service.LoginService;
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
}
