package com.xiongsu.service.user.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiongsu.api.vo.user.dto.UserStatisticInfoDTO;
import com.xiongsu.core.cache.RedisClient;
import org.springframework.stereotype.Component;

/**
 * @program: pai_coding
 * @description: 用户信息缓存
 * @author: XuYifei
 * @create: 2024-10-24
 */

@Component
public class UserInfoCacheManager {


    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();


    public static final String USER_INFO_PREFIX = "user_info:";

    /**
     * 获取用户信息缓存
     *
     */
    public UserStatisticInfoDTO getUserInfo(long userId) {
        Object value = RedisClient.getObject(USER_INFO_PREFIX + userId);
        if(value != null) {
            return OBJECT_MAPPER.convertValue(value, UserStatisticInfoDTO.class);
        }
        return null;
    }


    /**
     * 设置用户信息缓存
     */
    public void setUserInfo(long userId, UserStatisticInfoDTO userStatisticInfoDTO) {
        RedisClient.setObject(USER_INFO_PREFIX + userId, userStatisticInfoDTO);
    }

    /**
     * 删除用户信息缓存
     */
    public void delUserInfo(long userId) {
        RedisClient.delObject(USER_INFO_PREFIX + userId);
    }
}
