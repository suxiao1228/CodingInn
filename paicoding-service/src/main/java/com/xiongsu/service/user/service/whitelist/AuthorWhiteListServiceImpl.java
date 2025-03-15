package com.xiongsu.service.user.service.whitelist;


import com.xiongsu.core.cache.RedisClient;
import com.xiongsu.service.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthorWhiteListServiceImpl {
    /**
     *实用 Redis - set 来存储允许直接发文章的白名单
     */
    private static final String ARTICLE_WHITE_LIST = "auth_article_white_list";

    @Autowired
    private UserService userService;

    //@Override
    public boolean authorInArticleWhiteList(Long authorId) {
        return RedisClient.sIsMember(ARTICLE_WHITE_LIST, authorId);
    }


}
