package com.xiongsu.service.user.service;


import com.xiongsu.api.enums.ai.AISourceEnum;
import com.xiongsu.api.vo.chat.ChatItemVo;
import com.xiongsu.api.vo.user.UserPwdLoginReq;

public interface UserAiService {
    /**
     * 保存聊天历史记录
     *
     * @param source
     * @param user
     * @param item
     */
    void pushChatItem(AISourceEnum source, Long user, ChatItemVo item);

    /**
     * 获取用户的最大聊天次数
     *
     * @param userId
     * @return
     */
    int getMaxChatCnt(Long userId);

    /**
     * 重建用户绑定的星球编号
     *
     * @param loginReq
     */

    void initOrUpdateAiInfo(UserPwdLoginReq loginReq);
}
