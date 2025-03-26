package com.xiongsu.service.user.service;

import com.xiongsu.api.enums.user.UserAIStatEnum;
import com.xiongsu.api.vo.PageVo;
import com.xiongsu.api.vo.user.SearchZsxqUserReq;
import com.xiongsu.api.vo.user.ZsxqUserPostReq;
import com.xiongsu.api.vo.user.dto.ZsxqUserInfoDTO;

import java.util.List;

/**
 * 作者白名单服务
 */
public interface ZsxqWhiteListService {

    PageVo<ZsxqUserInfoDTO> getList(SearchZsxqUserReq req);

    void operate(Long id, UserAIStatEnum operate);

    /**
     *  重置论坛用户的 AI 相关信息。
     * @param authorId
     */
    void reset(Integer authorId);

    void batchOperate(List<Long> ids, UserAIStatEnum operate);


    void update(ZsxqUserPostReq req);
}
