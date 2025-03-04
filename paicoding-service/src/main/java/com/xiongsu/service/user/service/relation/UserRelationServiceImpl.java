package com.xiongsu.service.user.service.relation;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xiongsu.api.context.ReqInfoContext;
import com.xiongsu.api.enums.FollowStateEnum;
import com.xiongsu.api.enums.NotifyTypeEnum;
import com.xiongsu.api.vo.PageListVo;
import com.xiongsu.api.vo.PageParam;
import com.xiongsu.api.vo.notify.NotifyMsgEvent;
import com.xiongsu.api.vo.user.UserRelationReq;
import com.xiongsu.api.vo.user.dto.FollowUserInfoDTO;
import com.xiongsu.core.util.SpringUtil;
import com.xiongsu.service.user.converter.UserConverter;
import com.xiongsu.service.user.repository.dao.UserRelationDao;
import com.xiongsu.service.user.repository.entity.UserRelationDO;
import com.xiongsu.service.user.service.UserRelationService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class UserRelationServiceImpl implements UserRelationService {
    @Resource
    private UserRelationDao userRelationDao;

    /**
     * 查询用户的关注列表
     *
     * @param userId
     * @param pageParam
     * @return
     */
    @Override
    public PageListVo<FollowUserInfoDTO> getUserFollowList(Long userId, PageParam pageParam) {
        List<FollowUserInfoDTO> userRelationList = userRelationDao.listUserFollows(userId, pageParam);
        return PageListVo.newVo(userRelationList, pageParam.getPageSize());
    }

    @Override
    public PageListVo<FollowUserInfoDTO> getUserFansList(Long userId, PageParam pageParam) {
        return null;
    }

    @Override
    public void updateUserFollowRelationId(PageListVo<FollowUserInfoDTO> followList, Long loginUserId) {

    }

    /**
     * 根据登录用户从给定用户列表中，找出已关注的用户id
     *
     * @param userIds    主用户列表
     * @param fansUserId 粉丝用户id
     * @return 返回fansUserId已经关注过的用户id列表
     */
    @Override
    public Set<Long> getFollowedUserId(List<Long> userIds, Long fansUserId) {
        return Set.of();
    }

    @Override
    public void saveUserRelation(UserRelationReq req) {
        Long currentUserId = ReqInfoContext.getReqInfo().getUserId();

        //查询是否存在
        UserRelationDO userRelationDO = userRelationDao.getUserRelationRecord(req.getUserId(), ReqInfoContext.getReqInfo().getUserId());
        if(userRelationDO == null) {
            userRelationDO =  UserConverter.toDO(req);
            userRelationDao.save(userRelationDO);
            //发布关注事件
            SpringUtil.publishEvent(new NotifyMsgEvent<>(this, NotifyTypeEnum.FOLLOW, userRelationDO));
            return;
        }
        //将是否关注状态重置
        userRelationDO.setFollowState(Long.valueOf(req.getFollowed() ? FollowStateEnum.FOLLOW.getCode() : FollowStateEnum.CANCEL_FOLLOW.getCode()));
        userRelationDao.updateById(userRelationDO);
        //发布关注，取消关注事件
        SpringUtil.publishEvent(new NotifyMsgEvent<>(this, req.getFollowed() ? NotifyTypeEnum.FOLLOW : NotifyTypeEnum.CANCEL_FOLLOW, userRelationDO));
    }

    public IPage<FollowUserInfoDTO> getUserFollowListPagination(Long userId, int currentPage, int pageSize) {

        return userRelationDao.listUserFollowsPagination(currentPage, pageSize, userId);
//        userRelationDOIPage.convert(UserConverter::toDTO);
    }

    public IPage<FollowUserInfoDTO> getUserFansListPagination(Long userId, int currentPage, int pageSize) {
        return userRelationDao.listUserFansPagination(currentPage, pageSize, userId);
    }
}
