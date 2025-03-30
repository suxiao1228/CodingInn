package com.xiongsu.service.sitemap.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.xiongsu.api.enums.ArticleEventEnum;
import com.xiongsu.api.event.ArticleMsgEvent;
import com.xiongsu.api.vo.article.dto.SimpleArticleDTO;
import com.xiongsu.core.cache.RedisClient;
import com.xiongsu.core.util.DateUtil;
import com.xiongsu.service.article.repository.dao.ArticleDao;
import com.xiongsu.service.article.repository.entity.ArticleDO;
import com.xiongsu.service.sitemap.constants.SitemapConstants;
import com.xiongsu.service.sitemap.model.SiteCntVo;
import com.xiongsu.service.sitemap.model.SiteMapVo;
import com.xiongsu.service.sitemap.model.SiteUrlVo;
import com.xiongsu.service.sitemap.service.SitemapService;
import com.xiongsu.service.statistics.service.CountService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SitemapServiceImpl implements SitemapService {
    @Value("${view.site.host:https://paicoding.com}")
    private String host;
    private static final int SCAN_SIZE = 100;

    private static final String SITE_MAP_CACHE_KEY = "sitemap";

    @Resource
    private ArticleDao articleDao;
    @Resource
    private CountService countService;

    /**
     * 查询站点地图
     * @return 返回站点地图
     */
    public SiteMapVo getSiteMap() {
        // key = 文章id, value = 最后更新时间
        Map<String, Long> siteMap = RedisClient.hGetAll(SITE_MAP_CACHE_KEY, Long.class);
        if (CollectionUtils.isEmpty(siteMap)) {
            // 首次访问时，没有数据，全量初始化
            initSiteMap();
        }
        siteMap = RedisClient.hGetAll(SITE_MAP_CACHE_KEY, Long.class);
        SiteMapVo vo = initBasicSite();
        if (CollectionUtils.isEmpty(siteMap)) {
            return vo;
        }

        for (Map.Entry<String, Long> entry : siteMap.entrySet()) {
            vo.addUrl(new SiteUrlVo(host + "/article/detail/" + entry.getKey(), DateUtil.time2utc(entry.getValue())));
        }
        return vo;
    }

    /**
     * fixme: 加锁初始化，更推荐的是采用分布式锁
     */
    //为什么要加锁？
    //initSiteMap 方法是 synchronized 的，意味着它是线程安全的，即同一时间只能有一个线程执行该方法。加锁的主要原因是：
    //
    //防止并发问题
    //
    //该方法会删除 Redis 中的 SITE_MAP_CACHE_KEY，然后重新批量添加数据。如果多个线程同时执行，会导致数据不一致，比如一个线程删除缓存后，另一个线程正在写入，而第一个线程随后覆盖了数据，可能导致数据丢失或重复。
    //
    //保证 lastId 的正确性
    //
    //lastId 在 while 循环中不断更新，多个线程并发执行时，它们可能会互相覆盖 lastId，导致查询数据重复或遗漏。
    //
    //避免 Redis 频繁读写冲突
    //
    //由于该方法频繁向 Redis 进行 del() 和 hMSet() 操作，多个线程并发写入可能导致 Redis 负载增加，影响系统性能。
    //
    //为什么更好的方案是用分布式锁？
    //initSiteMap 方法当前的 synchronized 关键字只对单个 JVM 进程内生效，如果服务是多实例部署（分布式环境），那么不同实例上的线程仍然可能同时执行该方法，导致并发问题。分布式锁能解决这个问题：
    //
    //保证分布式环境下的并发安全
    //
    //例如使用 Redis 的 SETNX 或 Redisson 来加锁，可以确保只有一个实例执行 initSiteMap()，其他实例等待锁释放后再执行。
    //
    //避免锁竞争影响应用性能
    //
    //synchronized 适用于单机，但在高并发情况下可能会导致线程长时间等待。如果用 Redis 分布式锁，可以让其他实例快速失败，而不是阻塞等待，提高系统吞吐量。
    //
    //防止任务重复执行
    //
    //如果多个实例同时运行，可能会导致站点地图的 Redis 缓存被反复删除、写入，影响数据一致性。分布式锁可以确保该任务在整个集群中只有一个实例执行，避免不必要的资源消耗。
    private synchronized void initSiteMap() {
        long lastId = 0L;
        RedisClient.del(SITE_MAP_CACHE_KEY);
        while (true) {
            List<SimpleArticleDTO> list = articleDao.getBaseMapper().listArticlesOrderById(lastId, SCAN_SIZE);
            // 刷新文章的统计信息
            list.forEach(s -> countService.refreshArticleStatisticInfo(s.getId()));

            // 刷新站点地图信息
            Map<String, Long> map = list.stream().collect(Collectors.toMap(s -> String.valueOf(s.getId()), s -> s.getCreateTime().getTime(), (a, b) -> a));
            RedisClient.hMSet(SITE_MAP_CACHE_KEY, map);
            if (list.size() < SCAN_SIZE) {
                break;
            }
            lastId = list.get(list.size() - 1).getId();
        }
    }

    /**
     * 分布式锁
     * @return
    private void initSiteMap() {
        RLock lock = redissonClient.getLock("initSiteMapLock");
        if (lock.tryLock()) {
            try {
                long lastId = 0L;
                RedisClient.del(SITE_MAP_CACHE_KEY);
                while (true) {
                    List<SimpleArticleDTO> list = articleDao.getBaseMapper().listArticlesOrderById(lastId, SCAN_SIZE);
                    list.forEach(s -> countService.refreshArticleStatisticInfo(s.getId()));

                    Map<String, Long> map = list.stream().collect(Collectors.toMap(s -> String.valueOf(s.getId()), s -> s.getCreateTime().getTime(), (a, b) -> a));
                    RedisClient.hMSet(SITE_MAP_CACHE_KEY, map);
                    if (list.size() < SCAN_SIZE) {
                        break;
                    }
                    lastId = list.get(list.size() - 1).getId();
                }
            } finally {
                lock.unlock();
            }
        }
    }*/

    private SiteMapVo initBasicSite() {
        SiteMapVo vo = new SiteMapVo();
        String time = DateUtil.time2utc(System.currentTimeMillis());
        vo.addUrl(new SiteUrlVo(host + "/", time));
        vo.addUrl(new SiteUrlVo(host + "/column", time));
        vo.addUrl(new SiteUrlVo(host + "/admin-view", time));
        return vo;
    }

    /**
     * 重新刷新站点地图
     */
    @Override
    public void refreshSitemap() {
        initSiteMap();
    }

    /**
     * 基于文章的上下线，自动更新站点地图
     *
     * @param event
     */
    @EventListener(ArticleMsgEvent.class)
    public void autoUpdateSiteMap(ArticleMsgEvent<ArticleDO> event) {
        ArticleEventEnum type = event.getType();
        if (type == ArticleEventEnum.ONLINE) {
            addArticle(event.getContent().getId());
        } else if (type == ArticleEventEnum.OFFLINE || type == ArticleEventEnum.DELETE) {
            rmArticle(event.getContent().getId());
        }
    }

    /**
     * 新增文章并上线
     *
     * @param articleId
     */
    private void addArticle(Long articleId) {
        RedisClient.hSet(SITE_MAP_CACHE_KEY, String.valueOf(articleId), System.currentTimeMillis());
    }

    /**
     * 删除文章、or文章下线
     *
     * @param articleId
     */
    private void rmArticle(Long articleId) {
        RedisClient.hDel(SITE_MAP_CACHE_KEY, String.valueOf(articleId));
    }


    /**
     * 采用定时器方案，每天5:15分刷新站点地图，确保数据的一致性
     */
    @Scheduled(cron = "0 15 5 * * ?")
    public void autoRefreshCache() {
        log.info("开始刷新sitemap.xml的url地址，避免出现数据不一致问题!");
        refreshSitemap();
        log.info("刷新完成！");
    }


    /**
     * 保存站点数据模型
     * <p>
     * 站点统计hash：
     * - visit_info:
     * ---- pv: 站点的总pv
     * ---- uv: 站点的总uv
     * ---- pv_path: 站点某个资源的总访问pv
     * ---- uv_path: 站点某个资源的总访问uv
     * - visit_info_ip:
     * ---- pv: 用户访问的站点总次数
     * ---- path_pv: 用户访问的路径总次数
     * - visit_info_20230822每日记录, 一天一条记录
     * ---- pv: 12  # field = 月日_pv, pv的计数
     * ---- uv: 5   # field = 月日_uv, uv的计数
     * ---- pv_path: 2 # 资源的当前访问计数
     * ---- uv_path: # 资源的当天访问uv
     * ---- pv_ip: # 用户当天的访问次数
     * ---- pv_path_ip: # 用户对资源的当天访问次数
     *
     * @param visitIp 访问者ip
     * @param path    访问的资源路径
     */
    @Override
    public void saveVisitInfo(String visitIp, String path) {
        String globalKey = SitemapConstants.SITE_VISIT_KEY;
        String day = SitemapConstants.day(LocalDate.now());

        String todayKey = globalKey + "_" + day;

        // 用户的全局访问计数+1
        Long globalUserVisitCnt = RedisClient.hIncr(globalKey + "_" + visitIp, "pv", 1);
        // 用户的当日访问计数+1
        Long todayUserVisitCnt = RedisClient.hIncr(todayKey, "pv_" + visitIp, 1);

        RedisClient.PipelineAction pipelineAction = RedisClient.pipelineAction();
        if (globalUserVisitCnt == 1) {
            // 站点新用户
            // 今日的uv + 1
            pipelineAction.add(todayKey, "uv"
                    , (connection, key, field) -> {
                        connection.hIncrBy(key, field, 1);
                    });
            pipelineAction.add(todayKey, "uv_" + path
                    , (connection, key, field) -> connection.hIncrBy(key, field, 1));

            // 全局站点的uv
            pipelineAction.add(globalKey, "uv", (connection, key, field) -> connection.hIncrBy(key, field, 1));
            pipelineAction.add(globalKey, "uv_" + path, (connection, key, field) -> connection.hIncrBy(key, field, 1));
        } else if (todayUserVisitCnt == 1) {
            // 判断是今天的首次访问，更新今天的uv+1
            pipelineAction.add(todayKey, "uv", (connection, key, field) -> connection.hIncrBy(key, field, 1));
            if (RedisClient.hIncr(todayKey, "pv_" + path + "_" + visitIp, 1) == 1) {
                // 判断是否为今天首次访问这个资源，若是，则uv+1
                pipelineAction.add(todayKey, "uv_" + path, (connection, key, field) -> connection.hIncrBy(key, field, 1));
            }

            // 判断是否是用户的首次访问这个path，若是，则全局的path uv计数需要+1
            if (RedisClient.hIncr(globalKey + "_" + visitIp, "pv_" + path, 1) == 1) {
                pipelineAction.add(globalKey, "uv_" + path, (connection, key, field) -> connection.hIncrBy(key, field, 1));
            }
        }


        // 更新pv 以及 用户的path访问信息
        // 今天的相关信息 pv
        pipelineAction.add(todayKey, "pv", (connection, key, field) -> connection.hIncrBy(key, field, 1));
        pipelineAction.add(todayKey, "pv_" + path, (connection, key, field) -> connection.hIncrBy(key, field, 1));
        if (todayUserVisitCnt > 1) {
            // 非当天首次访问，则pv+1; 因为首次访问时，在前面更新uv时，已经计数+1了
            pipelineAction.add(todayKey, "pv_" + path + "_" + visitIp, (connection, key, field) -> connection.hIncrBy(key, field, 1));
        }


        // 全局的 PV
        pipelineAction.add(globalKey, "pv", (connection, key, field) -> connection.hIncrBy(key, field, 1));
        pipelineAction.add(globalKey, "pv" + "_" + path, (connection, key, field) -> connection.hIncrBy(key, field, 1));

        // 保存访问信息
        pipelineAction.execute();
        if (log.isDebugEnabled()) {
            log.info("用户访问信息更新完成! 当前用户总访问: {}，今日访问: {}", globalUserVisitCnt, todayUserVisitCnt);
        }
    }

    /**
     * 查询站点某一天or总的访问信息
     *
     * @param date 日期，为空时，表示查询所有的站点信息
     * @param path 访问路径，为空时表示查站点信息
     * @return
     */
    public SiteCntVo querySiteVisitInfo(LocalDate date, String path) {
        String globalKey = SitemapConstants.SITE_VISIT_KEY;
        String day = null, todayKey = globalKey;
        if (date != null) {
            day = SitemapConstants.day(date);
            todayKey = globalKey + "_" + day;
        }

        String pvField = "pv", uvField = "uv";
        if (path != null) {
            // 表示查询对应路径的访问信息
            pvField += "_" + path;
            uvField += "_" + path;
        }

        Map<String, Integer> map = RedisClient.hMGet(todayKey, Arrays.asList(pvField, uvField), Integer.class);
        SiteCntVo siteInfo = new SiteCntVo();
        siteInfo.setDay(day);
        siteInfo.setPv(map.getOrDefault(pvField, 0));
        siteInfo.setUv(map.getOrDefault(uvField, 0));
        return siteInfo;
    }
}
