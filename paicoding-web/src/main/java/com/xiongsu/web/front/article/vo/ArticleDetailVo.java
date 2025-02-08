package com.xiongsu.web.front.article.vo;

import com.xiongsu.api.vo.article.dto.ArticleDTO;
import com.xiongsu.api.vo.article.dto.ArticleOtherDTO;
import com.xiongsu.api.vo.article.dto.ArticlePayInfoDTO;
import com.xiongsu.api.vo.recommend.SideBarDTO;
import com.xiongsu.api.vo.user.dto.SimpleUserInfoDTO;
import com.xiongsu.api.vo.user.dto.UserStatisticInfoDTO;
import com.xiongsu.service.comment.converter.dto.TopCommentDTO;
import lombok.Data;

import java.util.List;

@Data
public class ArticleDetailVo {
    /**
     * 文章信息
     */
    private ArticleDTO article;

    /**
     * 评论信息
     */
    private List<TopCommentDTO> comments;

    /**
     * 热门评论
     */
    private TopCommentDTO hotComment;

    /**
     * 作者相关信息
     */
    private UserStatisticInfoDTO author;


    private ArticlePayInfoDTO payInfo;

    // 其他的信息，比如说翻页，比如说阅读类型
    private ArticleOtherDTO other;

    /**
     * 侧边栏信息
     */
    private List<SideBarDTO> sideBarItems;


    /**
     * 打赏用户列表
     */
    private List<SimpleUserInfoDTO> payUsers;
}
