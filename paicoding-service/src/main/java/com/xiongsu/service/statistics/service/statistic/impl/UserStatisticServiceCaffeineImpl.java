package com.xiongsu.service.statistics.service.statistic.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.xiongsu.service.statistics.service.statistic.UserStatisticService;
import org.springframework.beans.factory.annotation.Autowired;

public class UserStatisticServiceCaffeineImpl implements UserStatisticService {

    @Autowired
    private Cache<String, Boolean> sessionCache;

    @Override
    public void invalidateSession(String sessionStr) {
        sessionCache.invalidate(sessionStr);
    }

    @Override
    public int getOnlineUserCnt() {
        return (int) sessionCache.estimatedSize();
    }

    @Override
    public boolean isOnline(String sessionStr) {
        return sessionCache.asMap().containsKey(sessionStr);
    }

    @Override
    public void updateSessionExpireTime(String sessionStr) {
        sessionCache.put(sessionStr, true);
    }
}
