package com.xiongsu.service.user.service.relation;


import com.xiongsu.api.vo.PageListVo;
import com.xiongsu.api.vo.PageParam;
import com.xiongsu.api.vo.user.UserRelationReq;
import com.xiongsu.api.vo.user.dto.FollowUserInfoDTO;
import com.xiongsu.service.user.repository.dao.UserRelationDao;
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

    @Override
    public Set<Long> getFollowedUserId(List<Long> userIds, Long loginUserId) {
        return Set.of();
    }

    @Override
    public void saveUserRelation(UserRelationReq req) {

    }
}
