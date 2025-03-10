package com.xiongsu.service.sidebar.service;


import com.google.common.base.Splitter;
import com.ibm.icu.impl.coll.Collation;
import com.xiongsu.api.enums.ConfigTypeEnum;
import com.xiongsu.api.enums.SidebarStyleEnum;
import com.xiongsu.api.enums.rank.ActivityRankTimeEnum;
import com.xiongsu.api.vo.PageListVo;
import com.xiongsu.api.vo.PageParam;
import com.xiongsu.api.vo.article.dto.SimpleArticleDTO;
import com.xiongsu.api.vo.banner.dto.ConfigDTO;
import com.xiongsu.api.vo.rank.dto.RankItemDTO;
import com.xiongsu.api.vo.recommend.SideBarDTO;
import com.xiongsu.api.vo.recommend.SideBarItemDTO;
import com.xiongsu.service.article.repository.dao.ArticleDao;
import com.xiongsu.service.article.service.ArticleReadService;
import com.xiongsu.service.config.service.ConfigService;
import com.xiongsu.service.rank.service.UserActivityRankService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SidebarServiceImpl implements SidebarService{
    @Autowired
    private ArticleReadService articleReadService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private ArticleDao articleDao;


    @Autowired
    private UserActivityRankService userActivityRankService;

    /**
     * 使用caffeine本地缓存，来处理侧边栏不怎么变动的消息
     * <p>
     * cacheNames -> 类似缓存前缀的概念
     * key -> SpEL 表达式，可以从传参中获取，来构建缓存的key
     * cacheManager -> 缓存管理器，如果全局只有一个时，可以省略
     *
     * @return
     */
    @Override
    @Cacheable(key = "'homeSidebar'", cacheManager = "caffeineCacheManager", cacheNames = "home")
    public List<SideBarDTO> queryHomeSidebarList() {
        List<SideBarDTO> list = new ArrayList<>();
        list.add(noticeSideBar());
        list.add(columnSideBar());
        list.add(hotArticles());
        SideBarDTO bar = rankList();
        if (bar != null) {
        list.add(bar);
        }
        return list;
    }

    /**
     * 公告信息
     * @return
     */
    private SideBarDTO noticeSideBar() {
        List<ConfigDTO> noticleList = configService.getConfigList(ConfigTypeEnum.NOTICE);
        List<SideBarItemDTO> items = new ArrayList<>(noticleList.size());
        noticleList.forEach(configDTO -> {
            List<Integer> configTags;
            if (StringUtils.isBlank(configDTO.getTags())) {
                configTags = Collections.emptyList();
            } else{
                configTags = Splitter.on(",").splitToStream(configDTO.getTags()).map(s -> Integer.parseInt(s.trim())).collect(Collectors.toList());
            }
            items.add(new SideBarItemDTO()
                    .setName(configDTO.getName())
                    .setTitle(configDTO.getContent())
                    .setUrl(configDTO.getJumpUrl())
                    .setTime(configDTO.getCreateTime().getTime())
                    .setTags(configTags)
            );
        });
        return new SideBarDTO()
                .setTitle("关于编程客栈")
                .setItems(items)
                .setStyle(SidebarStyleEnum.NOTICE.getStyle());
    }

    /**
     * 推荐教程的侧边栏
     *
     * @return
     */
    private SideBarDTO columnSideBar() {
        List<ConfigDTO> columnList = configService.getConfigList(ConfigTypeEnum.COLUMN);
        List<SideBarItemDTO> items = new ArrayList<>(columnList.size());
        columnList.forEach(configDTO -> {
            SideBarItemDTO item = new SideBarItemDTO();
            item.setName(configDTO.getName());
            item.setTitle(configDTO.getContent());
            item.setUrl(configDTO.getJumpUrl());
            item.setImg(configDTO.getBannerUrl());
            items.add(item);
        });
        return new SideBarDTO().setTitle("精选教程").setItems(items).setStyle(SidebarStyleEnum.COLUMN.getStyle());
    }

    /**
     * 热门文章
     *
     * @return
     */
    private SideBarDTO hotArticles() {
        PageListVo<SimpleArticleDTO> vo = articleReadService.queryHotArticlesForRecommend(PageParam.newPageInstance(1, 8));
        List<SideBarItemDTO> items = vo.getList().stream().map(s -> new SideBarItemDTO().setTitle(s.getTitle()).setUrl("/article/detail/" + s.getId()).setTime(s.getCreateTime().getTime())).collect(Collectors.toList());
        return new SideBarDTO().setTitle("热门文章").setItems(items).setStyle(SidebarStyleEnum.ARTICLES.getStyle());
    }


    /**
     * 查询教程的侧边栏信息
     * @return
     */
    @Override
    @Cacheable(key = "'columnSidebar'", cacheManager = "caffeineCacheMapper", cacheNames = "column")
    public List<SideBarDTO> queryColumnSidebarList() {
        List<SideBarDTO> list = new ArrayList<>();
        list.add(subscribeSideBar());
        return list;
    }


    /**
     * 以用户 + 文章维度进行缓存设置
     * @param author
     * @param articleId
     * @return
     */
    @Override
    @Cacheable(key = "'sideBar_' + #articleId", cacheManager = "caffeineCacheManager", cacheNames = "article")
    public List<SideBarDTO> queryArticleDetailSidebarList(Long author, Long articleId) {
        List<SideBarDTO> list = new ArrayList<>(2);
        // 不能直接使用 pdfSideBar()的方式调用，会导致缓存不生效
        list.add(recommendByAuthor(author, articleId, PageParam.DEFAULT_PAGE_SIZE));
        return list;
    }

    /**
     * 作者的文章列表推荐
     * @param authorId
     * @param articleId
     * @param size
     * @return
     */
    private SideBarDTO recommendByAuthor(Long authorId, Long articleId, long size) {
        List<SimpleArticleDTO> list = articleDao.listAuthorHotArticles(authorId, PageParam.newPageInstance(PageParam.DEFAULT_PAGE_NUM, size));
        List<SideBarItemDTO> items = list.stream().filter(s -> !s.getId().equals(authorId))
                .map(s -> new SideBarItemDTO()
                        .setTitle(s.getTitle()).setUrl("/article/detail/" + s.getId())
                        .setTime(s.getCreateTime().getTime()))
                .collect(Collectors.toList());
        return new SideBarDTO().setTitle("相关文章").setItems(items).setStyle(SidebarStyleEnum.ARTICLES.getStyle());
    }

    /**
     * 订阅公众号
     *
     * @return
     */
    private SideBarDTO subscribeSideBar() {
        return new SideBarDTO().setTitle("订阅").setSubTitle("楼仔")
                .setImg("//cdn.tobebetterjavaer.com/paicoding/a768cfc54f59d4a056f79d1c959dcae9.jpg")
                .setContent("10本校招必刷八股文")
                .setStyle(SidebarStyleEnum.SUBSCRIBE.getStyle());
    }

    /**
     * 排行榜
     */
    private SideBarDTO rankList() {
        List<RankItemDTO> list = userActivityRankService.queryRankList(ActivityRankTimeEnum.MONTH, 8);
        if (list.isEmpty()) {
            return null;
        }
    }
}
