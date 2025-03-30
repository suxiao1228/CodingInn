package com.xiongsu.service.statistics.service.statistic.impl;

import com.xiongsu.service.statistics.service.statistic.UserStatisticService;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 用户统计服务
 */
public class UserStatisticServiceAtomicIntegerImpl implements UserStatisticService {

    /**
     * 添加在线人数
     *
     * @param add 正数，表示添加在线人数；负数，表示减少在线人数
     * @return
     */
    public void incrOnlineUserCnt(int add) {
        onlineUserCnt.addAndGet(add);
    }

    /**
     * 查询在线用户人数
     * @return
     */
    @Override
    public int getOnlineUserCnt() {
        return onlineUserCnt.get();
    }

    @Override
    public boolean isOnline(String sessionStr) {
        return false;
    }

    @Override
    public void updateSessionExpireTime(String sessionStr) {

    }
    /**
     * 对于单机的场景，可以直接使用本地局部变量来实现计数
     * 对于集群的场景，可考虑借助 redis的zset 来实现集群的在线用户人数统计
     */
    private AtomicInteger onlineUserCnt = new AtomicInteger(0);
}
