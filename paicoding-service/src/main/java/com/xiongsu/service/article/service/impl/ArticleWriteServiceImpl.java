package com.xiongsu.service.article.service.impl;

import com.xiongsu.api.context.ReqInfoContext;
import com.xiongsu.api.enums.*;
import com.xiongsu.api.event.ArticleMsgEvent;
import com.xiongsu.api.exception.ExceptionUtil;
import com.xiongsu.api.vo.constants.StatusEnum;
import com.xiongsu.api.vo.user.dto.BaseUserInfoDTO;
import com.xiongsu.core.permission.UserRole;
import com.xiongsu.core.util.SpringUtil;
import com.xiongsu.core.util.id.IdUtil;
import com.xiongsu.api.vo.article.ArticlePostReq;
import com.xiongsu.core.util.NumUtil;
import com.xiongsu.service.article.conveter.ArticleConverter;
import com.xiongsu.service.article.repository.dao.ArticleDao;
import com.xiongsu.service.article.repository.dao.ArticleTagDao;
import com.xiongsu.service.article.repository.entity.ArticleDO;
import com.xiongsu.service.article.service.ArticleWriteService;
import com.xiongsu.service.article.service.ColumnSettingService;
import com.xiongsu.service.user.service.AuthorWhiteListService;
import com.xiongsu.service.user.service.UserFootService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Date;
import java.util.Objects;
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

    //todo 到时候记得实现图片的逻辑类
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

        // 2.保存文章内容
        articleDao.saveArticleContent(articleId, content);

        //3.保存文章标签
        articleTagDao.batchSave(articleId, tags);

        // 发布文章，阅读计数+1
        userFootService.saveOrUpdateUserFoot(DocumentTypeEnum.ARTICLE, articleId, article.getUserId(), article.getUserId(), OperateTypeEnum.READ);

        // todo 事件发布这里可以进行优化，一次发送多个事件？ 或者借助bit知识点来表示多种事件状态
        // todo 这里两种方式可以优化，可以看笔记
        // 发布文章创建事件
        SpringUtil.publishEvent(new ArticleMsgEvent<>(this, ArticleEventEnum.CREATE, article));
        //文章直接上线时，发布上线事件
        SpringUtil.publishEvent(new ArticleMsgEvent<>(this, ArticleEventEnum.ONLINE, article));
        return articleId;
    }

    /**
     * 更新文章
     * @param article
     * @param content
     * @param tags
     * @return
     */

    private Long updateArticle(ArticleDO article, String content, Set<Long> tags) {
        // fixme 待补充文章的历史版本支持：若文章处于审核状态，则直接更新上一条记录；否则新插入一条记录
        boolean review = article.getStatus().equals(PushStatusEnum.REVIEW.getCode());
        if (needToReview(article)) {
            article.setStatus(PushStatusEnum.REVIEW.getCode());
        }
        //更新文章
        article.setUpdateTime(new Date());
        articleDao.updateById(article);

        //更新内容
        articleDao.updateArticleContent(article.getId(), content, review);

        // 标签更新
        if (tags != null && tags.size() > 0) {
            articleTagDao.updateTags(article.getId(), tags);
        }

        //发布文章待审核事件
        if (article.getStatus() == PushStatusEnum.ONLINE.getCode()) {
            //修改之后依然直接上线（对于白名单作者而言）
            SpringUtil.publishEvent(new ArticleMsgEvent<>(this, ArticleEventEnum.ONLINE, article));
        } else if (review) {
            // 非白名单作者， 修改再审核中的文章， 依然是待审核状态
            SpringUtil.publishEvent(new ArticleMsgEvent<>(this, ArticleEventEnum.REVIEW, article));
        }
        return article.getId();
    }

    /**
     * 非白名单的用户，发布的文章需要先进行审核
     * @param article
     * @return
     */
    private boolean needToReview(ArticleDO article) {
        // 把admin用户加入白名单
        BaseUserInfoDTO user = ReqInfoContext.getReqInfo().getUser();
        if (user.getRole() != null && user.getRole().equalsIgnoreCase(UserRole.ADMIN.name())) {
            return false;
        }
        return article.getStatus() == PushStatusEnum.ONLINE.getCode() && !articleWhiteListService.authorInArticleWhiteList(article.getUserId());
    }

    /**
     * 删除文章
     * @param articleId   文章id
     * @param loginUserId 执行操作的用户
     */
    @Override
    public void deleteArticle(Long articleId, Long loginUserId) {
        ArticleDO dto = articleDao.getById(articleId);
        if (dto != null && !Objects.equals(dto.getUserId(), loginUserId)) {
            // 没有权限
            throw ExceptionUtil.of(StatusEnum.FORBID_ERROR_MIXED, "请确认文章是否属于您!");
        }

        if (dto != null && dto.getDeleted() != YesOrNoEnum.YES.getCode()) {
            dto.setDeleted(YesOrNoEnum.YES.getCode());
            articleDao.updateById(dto);

            //发布文章删除事件
            SpringUtil.publishEvent(new ArticleMsgEvent<>(this, ArticleEventEnum.DELETE, dto));
        }

    }
}
