package com.xiongsu.service.user.repository.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiongsu.api.enums.YesOrNoEnum;
import com.xiongsu.api.vo.PageParam;
import com.xiongsu.api.vo.user.dto.ZsxqUserInfoDTO;
import com.xiongsu.service.user.repository.entity.UserAiDO;
import com.xiongsu.service.user.repository.mapper.UserAiMapper;
import com.xiongsu.service.user.repository.params.SearchZsxqWhiteParams;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserAiDao extends ServiceImpl<UserAiMapper, UserAiDO>  {

    @Resource
    private UserAiMapper userAiMapper;

    @Resource
    private UserDao userDao;

    public List<ZsxqUserInfoDTO> listZsxqUsersByParams(SearchZsxqWhiteParams params) {
        return userAiMapper.listZsxqUsersByParams(params,
                PageParam.newPageInstance(params.getPageNum(), params.getPageSize()));
    }

    public Long countZsxqUserByParams(SearchZsxqWhiteParams params) {
        return userAiMapper.countZsxqUsersByParams(params);
    }


    public void batchUpdateState(List<Long> ids, Integer code) {
        LambdaUpdateWrapper<UserAiDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(UserAiDO::getId, ids).set(UserAiDO::getState, code);
        userAiMapper.update(null, updateWrapper);
    }

    /**
     * 根据星球编号反查用户
     *
     * @param starNumber
     * @return
     */
    public UserAiDO getByStarNumber(String starNumber) {
        LambdaQueryWrapper<UserAiDO> queryUserAi = Wrappers.lambdaQuery();

        queryUserAi.eq(UserAiDO::getStarNumber, starNumber)
                .eq(UserAiDO::getDeleted, YesOrNoEnum.NO.getCode())
                .last("limit 1");
        return userAiMapper.selectOne(queryUserAi);
    }
}
