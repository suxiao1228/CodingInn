package com.xiongsu.web.front.article.extra;

import com.xiongsu.api.context.ReqInfoContext;
import com.xiongsu.api.enums.ArticleReadTypeEnum;
import com.xiongsu.api.vo.article.dto.ArticleDTO;
import com.xiongsu.api.vo.user.dto.BaseUserInfoDTO;
import com.xiongsu.service.article.service.ArticlePayService;
import com.xiongsu.web.config.GlobalViewConfig;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.function.Supplier;

/**
 * 用于控制文章阅读模式
 */
public class ArticleReadViewServiceExtend {

    @Autowired
    private GlobalViewConfig globalViewConfig;
    @Autowired
    private ArticlePayService articlePayService;



    public String formatArticleReadType(ArticleDTO article) {
        ArticleReadTypeEnum readType = ArticleReadTypeEnum.typeOf(article.getReadType());
        if(readType != null && readType != ArticleReadTypeEnum.NORMAL) {
            BaseUserInfoDTO user = ReqInfoContext.getReqInfo().getUser();
            if (readType == ArticleReadTypeEnum.STAR_READ) {
                // 星球用户阅读
                return mark(article, () -> user != null && (user.getUserId().equals(article.getAuthor())
                                || user.getStarStatus() == UserAIStatEnum.FORMAL),
                        globalViewConfig::getZsxqArticleReadCount);
            } else if (readType == ArticleReadTypeEnum.PAY_READ) {
                // 付费阅读
                return mark(article, () -> user != null && (user.getUserId().equals(article.getAuthor())
                                || articlePayService.hasPayed(article.getArticleId(), user.getUserId())),
                        globalViewConfig::getNeedPayArticleReadCount);
            } else if (readType == ArticleReadTypeEnum.LOGIN) {
                // 登录阅读
                return mark(article, () -> user != null, globalViewConfig::getNeedLoginArticleReadCount);
            }
        }
    }

    private String mark(ArticleDTO article, Supplier<Boolean> condition, Supplier<String> percent) {
        if (condition.get()) {
            // 可以阅读
            article.setCanRead(true);
            return article.getContent();
        } else {
            // 不能阅读
            article.setCanRead(false);
            return article.getContent()
                    .substring(0, (int) (article.getContent().length() * Float.parseFloat(percent.get()) / 100));
        }
    }
}
