package com.xiongsu.service.article.service.impl;

import com.xiongsu.core.util.id.IdUtil;
import com.xiongsu.api.enums.PushStatusEnum;
import com.xiongsu.api.vo.article.ArticlePostReq;
import com.xiongsu.core.util.NumUtil;
import com.xiongsu.service.article.conveter.ArticleConverter;
import com.xiongsu.service.article.repository.dao.ArticleDao;
import com.xiongsu.service.article.repository.dao.ArticleTagDao;
import com.xiongsu.service.article.repository.entity.ArticleDO;
import com.xiongsu.service.article.service.ArticleWriteService;
import com.xiongsu.service.user.service.UserFootService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Set;


@Slf4j
@Service
public class ArticleWriteServiceImpl implements ArticleWriteService {

    private final ArticleDao articleDao;

    private final ArticleTagDao articleTagDao;

    @Autowired
    private ColumnSettingService columnSettingService;

    @Autowired
    private UserFootService userFootService;

    @Autowired
    private ImageService imageService;

    //主要用于 编程式事务管理，它提供了一种 手动控制事务 的方式，而不是使用 @Transactional 进行声明式事务管理。
    @Resource
    private TransactionTemplate transactionTemplate;

    @Autowired
    private AuthorWhiteListService articleWhiteListService;

    public ArticleWriteServiceImpl(ArticleDao articleDao, ArticleTagDao articleTagDao) {
        this.articleDao = articleDao;
        this.articleTagDao = articleTagDao;
    }


    /**
     * 保存文章，当articleId存在时，表示更新记录；不存在时，表示插入
     * @param req    上传的文章体
     * @param author 作者
     * @return
     */
    @Override
    public Long saveArticle(ArticlePostReq req, Long author) {
        ArticleDO article = ArticleConverter.toArticleDo(req, author);
        String content = imageService.mdImgReplace(req.getContent());
        return transactionTemplate.execute(new TransactionCallback<Long>() {
            @Override
            public Long doInTransaction(TransactionStatus status) {
                Long articleId;
                if (NumUtil.nullOrZero(req.getArticleId())) {
                    articleId = insertArticle(article, content, req.getTagIds());
                    log.info("文章发布成功! title={}", req.getTitle());
                } else {
                    articleId = updateArticle(article, content, req.getTagIds());
                    log.info("文章更新成功！ title={}", article.getTitle());
                }
                if (req.getColumnId() != null) {
                    // 更新文章对应的专栏信息
                    columnSettingService.saveColumnArticle(articleId, req.getColumnId());
                }
                return articleId;
            }
        });
    }

    /**
     * 新建文章
     * @param article
     * @param content
     * @param tags
     * @return
     */
    private Long insertArticle(ArticleDO article, String content, Set<Long> tags) {
        // article + article_detail + tag  三张表的数据变更
        if (needToReview(article)) {
            // 非白名单中的作者发布文章需要及逆行审核
            article.setStatus(PushStatusEnum.REVIEW.getCode());
        }

        //1.保存文章
        // 使用分布式id生成文章主键
        Long articleId = IdUtil.genId();
        article.setId(articleId);
        articleDao.saveOrUpdate(article);
    }

    @Override
    public void deleteArticle(Long articleId, Long loginUserId) {

    }
}
