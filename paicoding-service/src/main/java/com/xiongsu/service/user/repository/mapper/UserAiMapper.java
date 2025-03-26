package com.xiongsu.service.user.repository.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiongsu.api.vo.PageParam;
import com.xiongsu.api.vo.user.dto.ZsxqUserInfoDTO;
import com.xiongsu.service.user.repository.entity.UserAiDO;
import com.xiongsu.service.user.repository.params.SearchZsxqWhiteParams;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * ai用户登录mapper接口
 *
 * @author XuYifei
 * @date 2024-07-12
 */
public interface UserAiMapper extends BaseMapper<UserAiDO> {

    Long countZsxqUsersByParams(@Param("searchParams") SearchZsxqWhiteParams params);

    List<ZsxqUserInfoDTO> listZsxqUsersByParams(@Param("searchParams") SearchZsxqWhiteParams params,
                                                @Param("pageParam") PageParam newPageInstance);
}
