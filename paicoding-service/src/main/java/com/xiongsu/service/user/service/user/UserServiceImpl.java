package com.xiongsu.service.user.service.user;

import com.xiongsu.api.context.ReqInfoContext;
import com.xiongsu.api.exception.ExceptionUtil;
import com.xiongsu.api.vo.article.dto.YearArticleDTO;
import com.xiongsu.api.vo.constants.StatusEnum;
import com.xiongsu.api.vo.user.UserInfoSaveReq;
import com.xiongsu.api.vo.user.UserPwdLoginReq;
import com.xiongsu.api.vo.user.dto.BaseUserInfoDTO;
import com.xiongsu.api.vo.user.dto.SimpleUserInfoDTO;
import com.xiongsu.api.vo.user.dto.UserStatisticInfoDTO;
import com.xiongsu.service.article.repository.dao.ArticleDao;
import com.xiongsu.service.statistics.service.CountService;
import com.xiongsu.service.user.cache.UserInfoCacheManager;
import com.xiongsu.service.user.converter.UserConverter;
import com.xiongsu.service.user.repository.dao.UserDao;
import com.xiongsu.service.user.repository.dao.UserRelationDao;
import com.xiongsu.service.user.repository.entity.UserDO;
import com.xiongsu.service.user.repository.entity.UserInfoDO;
import com.xiongsu.service.user.repository.entity.UserRelationDO;
import com.xiongsu.service.user.service.UserService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class UserServiceImpl implements UserService {


    @Resource
    private UserRelationDao userRelationDao;
    @Resource
    private UserDao userDao;

    @Resource
    private UserInfoCacheManager userInfoCacheManager;
    @Resource
    private CountService countService;

    @Resource
    private ArticleDao articleDao;

    @Override
    public UserDO getWxUser(String wxuuid) {
        return null;
    }

    @Override
    public List<SimpleUserInfoDTO> searchUser(String userName) {
        return List.of();
    }

    @Override
    public void saveUserInfo(UserInfoSaveReq req) {
        UserInfoDO userInfoDO = UserConverter.toDO(req);
        userDao.updateUserInfo(userInfoDO);
    }

    @Override
    public BaseUserInfoDTO getAndUpdateUserIpInfoBySessionId(String session, String clientIp) {
        return null;
    }

    @Override
    public SimpleUserInfoDTO querySimpleUserInfo(Long userId) {
        return null;
    }

    @Override
    public BaseUserInfoDTO queryBasicUserInfo(Long userId) {
        return null;
    }

    @Override
    public List<SimpleUserInfoDTO> batchQuerySimpleUserInfo(Collection<Long> userIds) {
        return List.of();
    }

    @Override
    public List<BaseUserInfoDTO> batchQueryBasicUserInfo(Collection<Long> userIds) {
        List<UserInfoDO> users = userDao.getByUserIds(userIds);
        if (CollectionUtils.isEmpty(users)) {
            throw ExceptionUtil.of(StatusEnum.USER_EXISTS, "userId=" + userIds);
        }
        return users.stream().map(UserConverter::toDTO).collect(Collectors.toList());
    }

    @Override
    public UserStatisticInfoDTO queryUserInfoWithStatistic(Long userId) {
        UserStatisticInfoDTO userHomeDTO = userInfoCacheManager.getUserInfo(userId);
        if(userHomeDTO == null) {
            userHomeDTO = countService.queryUserStatisticInfo(userId);
            BaseUserInfoDTO userInfoDTO = queryBasicUserInfo(userId);
            userHomeDTO = UserConverter.toUserHomeDTO(userHomeDTO, userInfoDTO);
            //用户资料完整度
            int cnt = 0;
            if (StringUtils.isNotBlank(userHomeDTO.getCompany())) {
                ++cnt;
            }
            if (StringUtils.isNotBlank(userHomeDTO.getPosition())) {
                ++cnt;
            }
            if (StringUtils.isNotBlank(userHomeDTO.getProfile())) {
                ++cnt;
            }
            userHomeDTO.setInfoPercent(cnt * 100 / 3);

            //加入天数
            int joinDayCount = (int) ((System.currentTimeMillis() - userHomeDTO.getCreateTime()
                    .getTime()) / (1000 * 3600 * 24));
            userHomeDTO.setJoinDayCount(Math.max(1, joinDayCount));

            //创作历程
            List<YearArticleDTO> yearArticleDTOS = articleDao.listYearArticleByUserId(userId);
            userHomeDTO.setYearArticleList(yearArticleDTOS);

            userInfoCacheManager.setUserInfo(userId, userHomeDTO);
        }

        //是否关注
        Long followUserId = ReqInfoContext.getReqInfo().getUserId();
        if(followUserId != null) {
            UserRelationDO userRelationDO = userRelationDao.getUserRelationByUserId(userId, followUserId);
            userHomeDTO.setFollowed((userRelationDO == null) ? Boolean.FALSE : Boolean.TRUE);
        } else{
            userHomeDTO.setFollowed(Boolean.FALSE);
        }
        return userHomeDTO;
    }

    @Override
    public Long getUserCount() {
        return this.userDao.getUserCount();
    }

    @Override
    public void bindUserInfo(UserPwdLoginReq loginReq) {

    }
}
