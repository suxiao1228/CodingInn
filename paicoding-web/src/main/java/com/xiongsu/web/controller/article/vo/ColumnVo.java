package com.xiongsu.web.controller.article.vo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xiongsu.api.vo.PageListVo;
import com.xiongsu.api.vo.article.dto.ColumnDTO;
import com.xiongsu.api.vo.recommend.SideBarDTO;
import lombok.Data;

import java.util.List;

@Data
public class ColumnVo {
    /**
     * 专栏列表
     */
    private PageListVo<ColumnDTO> columns;

    /**
     * mybatis-plus分页
     * 专栏列表
     */
    private IPage<ColumnDTO> columnPage;

    /**
     * 侧边栏信息
     */
    private List<SideBarDTO> sideBarItems;

}
