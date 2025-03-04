package com.xiongsu.service.user.converter;


import com.xiongsu.api.context.ReqInfoContext;
import com.xiongsu.api.enums.FollowStateEnum;
import com.xiongsu.api.enums.RoleEnum;
import com.xiongsu.api.enums.user.UserAIStatEnum;
import com.xiongsu.api.vo.user.UserInfoSaveReq;
import com.xiongsu.api.vo.user.UserRelationReq;
import com.xiongsu.api.vo.user.UserSaveReq;
import com.xiongsu.api.vo.user.dto.BaseUserInfoDTO;
import com.xiongsu.api.vo.user.dto.SimpleUserInfoDTO;
import com.xiongsu.api.vo.user.dto.UserStatisticInfoDTO;
import com.xiongsu.service.user.repository.entity.UserDO;
import com.xiongsu.service.user.repository.entity.UserInfoDO;
import org.springframework.beans.BeanUtils;

/**
 * 用户转换
 */
public class UserConverter {

    public static UserDO toDO(UserSaveReq req) {
        if (req == null) {
            return null;
        }
        UserDO userDO = new UserDO();
        userDO.setId(req.getUserId());
        userDO.setThirdAccountId(req.getThirdAccountId());
        userDO.setLoginType(req.getLoginType());
        return userDO;
    }

    public static UserInfoDO toDO(UserInfoSaveReq req) {
        if (req == null) {
            return null;
        }
        UserInfoDO userInfoDO = new UserInfoDO();
        userInfoDO.setUserId(req.getUserId());
        userInfoDO.setUserName(req.getUserName());
        userInfoDO.setPhoto(req.getPhoto());
        userInfoDO.setPosition(req.getPosition());
        userInfoDO.setCompany(req.getCompany());
        userInfoDO.setProfile(req.getProfile());
        return userInfoDO;
    }

    public static BaseUserInfoDTO toDTO(UserInfoDO info, UserAiDO userAiDO) {
        BaseUserInfoDTO user = toDTO(info);
        if (userAiDO != null) {
            user.setStarStatus(UserAIStatEnum.fromCode(userAiDO.getState()));
        }
        return user;
    }

    public static BaseUserInfoDTO toDTO(UserInfoDO info) {
        if (info == null) {
            return null;
        }
        BaseUserInfoDTO user = new BaseUserInfoDTO();
        // todo 知识点，bean属性拷贝的几种方式， 直接get/set方式，使用BeanUtil工具类(spring, cglib, apache, objectMapper)，序列化方式等
        BeanUtils.copyProperties(info, user);
        // 设置用户最新登录地理位置
        user.setRegion(info.getIp().getLatestRegion());
        // 设置用户角色
        user.setRole(RoleEnum.role(info.getUserRole()));
        return user;
    }

    public static SimpleUserInfoDTO toSimpleInfo(UserInfoDO info) {
        return new SimpleUserInfoDTO().setUserId(info.getUserId())
                .setName(info.getUserName())
                .setAvatar(info.getPhoto())
                .setProfile(info.getProfile());
    }

    public static UserRelationDO toDO(UserRelationReq req) {
        if (req == null) {
            return null;
        }
        UserRelationDO userRelationDO = new UserRelationDO();
        userRelationDO.setUserId(req.getUserId());
        userRelationDO.setFollowUserId(ReqInfoContext.getReqInfo().getUserId());
        userRelationDO.setFollowState(req.getFollowed() ? FollowStateEnum.FOLLOW.getCode() : FollowStateEnum.CANCEL_FOLLOW.getCode());
        return userRelationDO;
    }

    public static UserStatisticInfoDTO toUserHomeDTO(UserStatisticInfoDTO userHomeDTO, BaseUserInfoDTO baseUserInfoDTO) {
        if (baseUserInfoDTO == null) {
            return null;
        }
        BeanUtils.copyProperties(baseUserInfoDTO, userHomeDTO);
        return userHomeDTO;
    }
}
