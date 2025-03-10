package com.xiongsu.service.sidebar.service;

import com.xiongsu.api.vo.recommend.SideBarDTO;

import java.util.List;

public interface SidebarService {

    /**
     * 查询首页的侧边栏消息
     * @return
     */
    List<SideBarDTO> queryHomeSidebarList();

    /**
     * 查询教程的侧边栏信息
     * @return
     */
    List<SideBarDTO> queryColumnSidebarList();

    /**
     * 查询文章详情的侧边栏信息
     * @param author
     * @param articleId
     * @return
     */
    List<SideBarDTO> queryArticleDetailSidebarList(Long author, Long articleId);
}
