package com.xiongsu.service.article.service.impl;


import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiongsu.api.enums.*;
import com.xiongsu.api.exception.ExceptionUtil;
import com.xiongsu.api.vo.PageListVo;
import com.xiongsu.api.vo.PageParam;
import com.xiongsu.api.vo.PageVo;
import com.xiongsu.api.vo.article.dto.ArticleDTO;
import com.xiongsu.api.vo.article.dto.CategoryDTO;
import com.xiongsu.api.vo.article.dto.SimpleArticleDTO;
import com.xiongsu.api.vo.article.dto.TagDTO;
import com.xiongsu.api.vo.constants.StatusEnum;
import com.xiongsu.api.vo.user.dto.BaseUserInfoDTO;
import com.xiongsu.core.cache.RedisClient;
import com.xiongsu.core.util.ArticleUtil;
import com.xiongsu.service.article.cache.ArticleCacheManager;
import com.xiongsu.service.article.conveter.ArticleConverter;
import com.xiongsu.service.article.repository.dao.ArticleDao;
import com.xiongsu.service.article.repository.dao.ArticleTagDao;
import com.xiongsu.service.article.repository.entity.ArticleDO;
import com.xiongsu.service.article.service.ArticleReadService;
import com.xiongsu.service.article.service.CategoryService;
import com.xiongsu.service.constant.RedisConstant;
import com.xiongsu.service.statistics.service.CountService;
import com.xiongsu.service.user.repository.entity.UserFootDO;
import com.xiongsu.service.user.service.UserFootService;
import com.xiongsu.service.user.service.UserService;
import com.xiongsu.service.utils.RedisLuaUtil;
import com.xiongsu.service.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.redisson.api.RedissonClient;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 文章查询相关服务类
 */
@Service
@Slf4j
public class ArticleReadServiceImpl implements ArticleReadService {
    @Autowired
    private ArticleDao articleDao;

    @Autowired
    private ArticleTagDao articleTagDao;


    @Autowired
    private ArticleReadService articleService;

    @Autowired
    private CategoryService categoryService;
    /**
     * 在一个项目中，UserFootService 就是内部服务调用
     * 拆微服务时，这个会作为远程服务访问
     */
    @Autowired
    private UserFootService userFootService;

    @Autowired
    private CountService countService;

    @Autowired
    private UserService userService;

    @Autowired
    private ArticleCacheManager articleCacheManager;

    // 是否开启ES
    @Value("${elasticsearch.open:false}")
    private Boolean openES;

    @Autowired
    private RedisLuaUtil redisLuaUtil;

    @Autowired(required = false)
    private RedissonClient redissonClient;

    @Value("${spring.redis.isOpen}")
    private Boolean openRedis;


    @Autowired
    private RedisUtil redisUtil;

    @Override
    public ArticleDO queryBasicArticle(Long articleId) {
        return articleDao.getById(articleId);
    }

    @Override
    public String generateSummary(String content) {
        return ArticleUtil.pickSummary(content);
    }

    @Override
    public PageVo<TagDTO> queryTagsByArticleId(Long articleId) {
        return null;
    }

//    @Override
//    public ArticleDTO queryDetailArticleInfo(Long articleId) {
//        ArticleDTO article = articleDao.queryArticleDetail(articleId);
//        if (article == null) {
//            throw ExceptionUtil.of(StatusEnum.ARTICLE_NOT_EXISTS, articleId);
//        }
//        // 更新分类相关信息
////        CategoryDTO category = article.getCategory();
////        category.setCategory(categoryService.queryCategoryName(category.getCategoryId()));
//
//        // 更新标签信息
//        article.setTags(articleTagDao.queryArticleTagDetails(articleId));
//        return article;
//    }
//    @Override
//    public ArticleDTO queryDetailArticleInfo(Long articleId) {
//        // TODO ygl:引入Redis缓存
//        ArticleDTO article = null;
//        // 兼容是否开启Redis
//        if (openRedis) {
//            String redisCacheKey = RedisConstant.REDIS_PRE_ARTICLE + RedisConstant.REDIS_CACHE + articleId;
//            String articleStr = RedisClient.getStr(redisCacheKey);
//            if (!ObjectUtils.isEmpty(articleStr)) {
//                article = JSONUtil.toBean(articleStr, ArticleDTO.class);
//            } else{
//                // TODO ygl:存在缓存击穿问题，引入分布式锁
//
//            /*
//            第一种方式：
//            缺点：不加finally去del锁，那么会出现该线程执行完之后在不过期时间内一直持有该锁不释放，
//            在这过程内导致其他线程无法再次获取锁
//            */
//                // article = this.checkArticleByDBOne(articleId);
//
//            /*
//            第二种方式：
//                优点：与第一种方式相比增加了finally，在线程执行完之后会立即释放锁
//            即使在执行finally之前宕机了，那么因为有了过期时间，还是会自动释放
//                缺点：可能会释放别人的锁。
//            */
//                // article = this.checkArticleByDBTwo(articleId);
//
//            /*
//            第三种方式：
//                优点：与第二种方式相比解决了其删除别人分布式锁的问题。在加锁时set(key, value);
//            解锁时会对比是否和他加锁时的value是否相等，相等则是他自己的锁，否则是别人锁不能解锁。
//                在解锁时采用了lua脚本保证其原子性
//                缺点：这种方式会出现加锁过期时间不能够根据业务和运行环境设置合适过期时间；
//            设置时间过短，则会业务还未执行完毕则锁自动释放，那么其他线程依旧可以拿到锁，无法很好解决缓存击穿问题
//            设置时间过长：如果在执行finally释放锁之前系统宕机了，那么还需要等着到时间后才能自动解锁
//            */
//                // article = this.checkArticleByDBThree(articleId);
//
//            /*
//            第四种方式：
//                优点：解决了第三种方式无法设置合适过期时间
//            */
//                article = this.checkArticleByDBFour(articleId);
//            }
//            if (article != null) {
//                RedisClient.setStr(redisCacheKey, JSONUtil.toJsonStr(article));
//            }
//        } else{
//            article = articleDao.queryArticleDetail(articleId);
//        }
//
//        if (article == null) {
//            throw ExceptionUtil.of(StatusEnum.ARTICLE_NOT_EXISTS, articleId);
//        }
//
//        // 更新分类相关信息
//        CategoryDTO category = article.getCategory();
//        category.setCategory(categoryService.queryCategoryName(category.getCategoryId()));
//
//        // 更新标签信息
//        article.setTags(articleTagDao.queryArticleTagDetails(articleId));
//        return article;
//    }
private final DistributedLockUtil lockUtil = new DistributedLockUtil(redissonClient);

    @Override
    public ArticleDTO queryDetailArticleInfo(Long articleId) {
        ArticleDTO article = null;
        if (openRedis) {
            String redisCacheKey = RedisConstant.REDIS_PRE_ARTICLE + RedisConstant.REDIS_CACHE + articleId;
            String articleStr = RedisClient.getStr(redisCacheKey);

            if (!ObjectUtils.isEmpty(articleStr)) {
                article = JSONUtil.toBean(articleStr, ArticleDTO.class);
            } else {
                String lockKey = RedisConstant.REDIS_PAI + RedisConstant.REDIS_PRE_ARTICLE
                        + RedisConstant.REDIS_LOCK + articleId;

                article = lockUtil.executeWithLock(lockKey, 3, 200, () -> {
                    // 双重检查缓存（防止并发穿透）
                    String cachedData = RedisClient.getStr(redisCacheKey);
                    if (!ObjectUtils.isEmpty(cachedData)) {
                        return JSONUtil.toBean(cachedData, ArticleDTO.class);
                    }

                    ArticleDTO dbData = articleDao.queryArticleDetail(articleId);
                    if (dbData != null) {
                        RedisClient.setStr(redisCacheKey, JSONUtil.toJsonStr(dbData));
                    }
                    return dbData;
                });
            }
        } else {
            article = articleDao.queryArticleDetail(articleId);
        }

        if (article == null) {
            throw ExceptionUtil.of(StatusEnum.ARTICLE_NOT_EXISTS, articleId);
        }

        // 更新分类相关信息
        CategoryDTO category = article.getCategory();
        category.setCategory(categoryService.queryCategoryName(category.getCategoryId()));

        // 更新标签信息
        article.setTags(articleTagDao.queryArticleTagDetails(articleId));
        return article;
    }

    public class DistributedLockUtil {
        private final RedissonClient redissonClient;

        public DistributedLockUtil(RedissonClient redissonClient) {
            this.redissonClient = redissonClient;
        }

        public <T> T executeWithLock(String lockKey, int maxRetries, long baseWaitTimeMs, Supplier<T> action) {
            RLock lock = redissonClient.getLock(lockKey);
            Random random = new Random();

            for (int attempt = 0; attempt < maxRetries; attempt++) {
                try {
                    // 尝试获取锁（不指定leaseTime以启用看门狗机制）
                    if (lock.tryLock(3, TimeUnit.SECONDS)) {
                        try {
                            return action.get(); // 执行实际业务逻辑
                        } finally {
                            lock.unlock();
                        }
                    } else {
                        // 指数退避 + 随机抖动
                        long waitTime = baseWaitTimeMs * (1 << attempt) + random.nextInt(100);
                        Thread.sleep(waitTime);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while acquiring lock", e);
                }
            }
            throw new RuntimeException("Failed to acquire lock after " + maxRetries + " attempts");
        }
    }

    /**
     * Redis分布式锁的第四种方法
     * @param articleId
     * @return
     */

    private ArticleDTO checkArticleByDBFour(Long articleId) {
        ArticleDTO article = null;
        String redisLockKey =
                RedisConstant.REDIS_PAI
                        + RedisConstant.REDIS_PRE_ARTICLE
                        + RedisConstant.REDIS_LOCK
                        + articleId;
        RLock lock = redissonClient.getLock(redisLockKey);
        //lock.lock();

        try {
            //尝试加锁,最大等待时间3秒，上锁30秒自动解锁
            if (lock.tryLock(3, 30, TimeUnit.SECONDS)) {
                article = articleDao.queryArticleDetail(articleId);
            } else {
                // 未获得分布式锁线程睡眠一下；然后再去获取数据
                Thread.sleep(200);
                this.queryDetailArticleInfo(articleId);//这里可能会栈溢出，所以可以改为循环，限制循环次数
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            //判断该lock是否已经锁 并且 锁是否是自己的
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }

        }
        return article;
    }

    /**
     * Redis分布式锁第三种方法
     *
     * @param articleId
     * @return ArticleDTO
     */
    private ArticleDTO checkArticleByDBThree(Long articleId) {

        String redisLockKey =
                RedisConstant.REDIS_PAI + RedisConstant.REDIS_PRE_ARTICLE + RedisConstant.REDIS_LOCK + articleId;

        String value = RandomUtil.randomString(6);
        Boolean isLockSuccess = redisUtil.setIfAbsent(redisLockKey, value, 90L);
        ArticleDTO article = null;
        try {
            if (isLockSuccess) {
                article = articleDao.queryArticleDetail(articleId);
            } else {
                Thread.sleep(200);
                this.queryDetailArticleInfo(articleId);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 这种先get出value，然后再比较删除；这无法保证原子性，为了保证原子性，采用了lua脚本
            /*
            String redisLockValue = RedisClient.getStr(redisLockKey);
            if (!ObjectUtils.isEmpty(redisLockValue) && StringUtils.equals(value, redisLockValue)) {
                RedisClient.del(redisLockKey);
            }
            */
            Long cad = redisLuaUtil.cad("pai_" + redisLockKey, value);
            log.info("lua 脚本删除结果：" + cad);


        }

        return article;

    }


    /**
     * Redis分布式锁第二种方法
     *
     * @param articleId
     * @return ArticleDTO
     */
    private ArticleDTO checkArticleByDBTwo(Long articleId) {

        String redisLockKey =
                RedisConstant.REDIS_PAI + RedisConstant.REDIS_PRE_ARTICLE + RedisConstant.REDIS_LOCK + articleId;

        ArticleDTO article = null;

        Boolean isLockSuccess = redisUtil.setIfAbsent(redisLockKey, null, 90L);
        try {
            if (isLockSuccess) {
                article = articleDao.queryArticleDetail(articleId);
            } else {
                Thread.sleep(200);
                this.queryDetailArticleInfo(articleId);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            RedisClient.del(redisLockKey);
        }

        return article;

    }

    /**
     * Redis分布式锁第一种方法
     *
     * @param articleId
     * @return ArticleDTO
     */
    private ArticleDTO checkArticleByDBOne(Long articleId) {

        String redisLockKey =
                RedisConstant.REDIS_PAI + RedisConstant.REDIS_PRE_ARTICLE + RedisConstant.REDIS_LOCK + articleId;

        ArticleDTO article = null;
        Boolean isLockSuccess = redisUtil.setIfAbsent(redisLockKey, null, 90L);

        if (isLockSuccess) {
            article = articleDao.queryArticleDetail(articleId);
        } else {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.queryDetailArticleInfo(articleId);
        }

        return article;
    }

    /**
     * 查询文章所有的关联信息，正文，分类，标签，阅读计数，当前登录用户是否点赞，评论过
     * @param articleId   文章id
     * @param readUser 当前查看的用户ID
     * @return
     */
    @Override
    public ArticleDTO queryFullArticleInfo(Long articleId, Long readUser) {
        ArticleDTO article;

        acticle = articleCacheManager.getArticleInfo(articleId);

        if (article == null) {
            article = queryDetailArticleInfo(articleId);
            articleCacheManager.setArticleInfo(articleId, article);
        }

        //文章阅读计数+1
        countService.incrArticleReadCount(article.getAuthor(), article);

        //文章的操作标记
        if (readUser != null) {
            //更新用于足迹，并判断是否点赞，评论，收藏
            UserFootDO foot = userFootService.saveOrUpdateUserFoot(DocumentTypeEnum.ARTICLE, articleId,
                    article.getAuthor(), readUser, OperateTypeEnum.READ);
            article.setPraised(Objects.equals(foot.getPraiseStat(), PraiseStatEnum.PRAISE.getCode()));
            article.setCommented(Objects.equals(foot.getCommentStat(), CommentStatEnum.COMMENT.getCode()));
            article.setCollected(Objects.equals(foot.getCollectionStat(), CollectionStatEnum.COLLECTION.getCode()));
        } else {
            // 未登录，全部设置为未处理
            article.setPraised(false);
            article.setCommented(false);
            article.setCollected(false);
        }

        //更新文章统计计数
        article.setCount(countService.queryArticleStatisticInfo(articleId));

        //设置文章的点赞列表
        article.setPraisedUsers(userFootService.queryArticlePraisedUsers(articleId));
        return article;
    }

    @Override
    public PageListVo<ArticleDTO> queryArticlesByCategory(Long categoryId, PageParam page) {
        List<ArticleDO> records = articleDao.listArticlesByCategoryId(categoryId, page);
        return buildArticleListVo(records, page.getPageSize());
    }

    @Override
    public IPage<ArticleDTO> queryArticlesByCategoryPagination(int currentPage, int pageSize, String category) {
        CategoryDTO categoryDto = categories(category);
        IPage<ArticleDO> records = articleDao.listArticlesByCategoryIdPagination(currentPage, pageSize, categoryDto.getCategoryId());
//        return buildArticleListVo(records, page.getPageSize());
        return records.convert(this::fillArticleRelatedInfo);
    }

    /**
     * 返回分类列表
     * @param active
     * @return
     */
    private CategoryDTO categories(String active) {
        List<CategoryDTO> allList = categoryService.loadAllCategories();
        //查询所有分类的对应的文章数
        Map<Long, Long> articleCnt = articleService.queryArticleCountsByCategory();
        // 过滤掉文章数为0的分类
        allList.removeIf(c -> articleCnt.getOrDefault(c.getCategoryId(), 0L) <= 0L);

        // 刷新选中的分类
        AtomicReference<CategoryDTO> selectedArticle = new AtomicReference<>();
        allList.forEach(category -> {
            if (category.getCategory().equalsIgnoreCase(active)) {
                selectedArticle.set(category);
            }
        });

        // 添加默认的全部分类
        allList.add(0, new CategoryDTO(0L, CategoryDTO.DEFAULT_TOTAL_CATEGORY));
        if (selectedArticle.get() == null) {
            selectedArticle.set(allList.get(0));
        }

        return selectedArticle.get();
    }

    /**
     * 分类根据tag查询
     * @param currentPage
     * @param pageSize
     * @param tagId
     * @return
     */
    @Override
    public IPage<ArticleDTO> queryArticlesByTagPagination(int currentPage, int pageSize, Long tagId) {
        IPage<ArticleDO> records = articleDao.listArticlesByTagIdPagination(currentPage, pageSize, tagId);

        return records.convert(this::fillArticleRelatedInfo);
    }

    @Override
    public List<ArticleDTO> queryTopArticlesByCategory(Long categoryId) {
        PageParam page = PageParam.newPageInstance(PageParam.DEFAULT_PAGE_NUM, PageParam.TOP_PAGE_SIZE);
        List<ArticleDO> articleDTOS = articleDao.listArticlesByCategoryId(categoryId, page);
        return articleDTOS.stream().map(this::fillArticleRelatedInfo).collect(Collectors.toList());
    }

    @Override
    public Long queryArticleCountByCategory(Long categoryId) {
        return 0L;
    }

    @Override
    public Map<Long, Long> queryArticleCountsByCategory() {
        return articleDao.countArticleByCategoryId();
    }

    @Override
    public PageListVo<ArticleDTO> queryArticlesByTag(Long tagId, PageParam page) {
        return null;
    }

    @Override
    public List<SimpleArticleDTO> querySimpleArticleBySearchKey(String key) {
        return List.of();
    }

    @Override
    public PageListVo<ArticleDTO> queryArticlesBySearchKey(String key, PageParam page) {
        return null;
    }

    @Override
    public PageListVo<ArticleDTO> queryArticlesByUserAndType(Long userId, PageParam pageParam, HomeSelectEnum select) {
        return null;
    }

    /**
     * 根据用户id分页查询用户浏览的历史文章
     * @param userId
     * @param currentPage
     * @param pageSize
     * @return
     */
    @Override
    public IPage<ArticleDTO> queryHistoryArticlesByUserIdPagination(Long userId, int currentPage, int pageSize) {
        Page<ArticleDO> page = new Page<>(currentPage, pageSize);
        IPage<ArticleDO> articleDOIPage = articleDao.listHistoryArticlesByUserIdPagination(page, userId);

        return articleDOIPage.convert(this::fillArticleRelatedInfo);
    }

    /**
     * 根据用户id分页查询用户收藏的文章
     * @param userId
     * @param currentPage
     * @param pageSize
     * @return
     */
    @Override
    public IPage<ArticleDTO> queryStarArticlesByUserIdPagination(Long userId, int currentPage, int pageSize) {
        Page<ArticleDO> page = new Page<>();
        IPage<ArticleDO> articleDOIPage = articleDao.listStarArticlesByUserIdPagination(page, userId);

        return articleDOIPage.convert(this::fillArticleRelatedInfo);
    }

    /**
     * 根据用户id分页查询用户发表的文章
     * @param userId
     * @param currentPage
     * @param pageSize
     * @return
     */
    @Override
    public IPage<ArticleDTO> queryArticlesByUserIdPagination(Long userId, int currentPage, int pageSize) {

        IPage<ArticleDO> articleDOIPage = articleDao.listArticlesByUserIdPagination(userId, currentPage, pageSize);

        return articleDOIPage.convert(this::fillArticleRelatedInfo);
    }

    /**
     * 补全文章的阅读计数、作者、分类、标签等信息
     *
     * @param record
     * @return
     */
    private ArticleDTO fillArticleRelatedInfo(ArticleDO record) {
        ArticleDTO dto = ArticleConverter.toDto(record);
        // 分类信息
        dto.getCategory().setCategory(categoryService.queryCategoryName(record.getCategoryId()));
        // 标签列表
        dto.setTags(articleTagDao.queryArticleTagDetails(record.getId()));
        // 阅读计数统计
        dto.setCount(countService.queryArticleStatisticInfo(record.getId()));
        // 作者信息
        BaseUserInfoDTO author = userService.queryBasicUserInfo(dto.getAuthor());
        dto.setAuthorName(author.getUserName());
        dto.setAuthorAvatar(author.getPhoto());
        return dto;
    }
    @Override
    public PageListVo<ArticleDTO> buildArticleListVo(List<ArticleDO> records, long pageSize) {
        return null;
    }

    @Override
    public PageListVo<SimpleArticleDTO> queryHotArticlesForRecommend(PageParam pageParam) {
        return null;
    }

    @Override
    public int queryArticleCount(long authorId) {
        return 0;
    }

    @Override
    public Long getArticleCount() {
        return 0L;
    }
}
