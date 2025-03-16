package com.xiongsu.service.article.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiongsu.api.exception.ExceptionUtil;
import com.xiongsu.api.vo.PageVo;
import com.xiongsu.api.vo.article.dto.ColumnDTO;
import com.xiongsu.api.vo.article.dto.SimpleArticleDTO;
import com.xiongsu.api.vo.constants.StatusEnum;
import com.xiongsu.service.article.cache.ArticleCacheManager;
import com.xiongsu.service.article.conveter.ColumnConvert;
import com.xiongsu.service.article.repository.dao.ArticleDao;
import com.xiongsu.service.article.repository.dao.ColumnArticleDao;
import com.xiongsu.service.article.repository.dao.ColumnDao;
import com.xiongsu.service.article.repository.entity.ColumnArticleDO;
import com.xiongsu.service.article.repository.entity.ColumnInfoDO;
import com.xiongsu.service.article.service.ColumnService;
import com.xiongsu.service.user.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class ColumnServiceImpl implements ColumnService {

    @Resource
    private ColumnDao columnDao;
    @Resource
    private ArticleDao articleDao;

    @Resource
    private ColumnArticleDao columnArticleDao;

    @Resource
    private UserService userService;

    @Resource
    private ArticleCacheManager articleCacheManager;

    @Override
    public ColumnArticleDO getColumnArticleRelation(Long articleId) {
        if(articleCacheManager.isArticleColumnArticleExist(articleId)) { //检查 缓存 是否存在该文章 ID 对应的 ColumnArticleDO 记录。
            return articleCacheManager.getColumnArticle(articleId);
        }
        ColumnArticleDO columnArticleDO = columnArticleDao.selectColumnArticleByArticleId(articleId);//执行数据库查询
        articleCacheManager.setColumnArticle(articleId, columnArticleDO);// 将数据库中的数据写入缓存
        return columnArticleDO;
    }

    @Override
    public IPage<ColumnDTO> listColumnByPage(Long currentPage, Long pageSize) {
        Page<ColumnInfoDO> columnInfoDOPage = columnDao.listOnlineColumnsByPage(currentPage, pageSize);
        return columnInfoDOPage.convert(this::buildColumnInfo);
    }

    @Override
    public ColumnDTO queryBasicColumnInfo(Long columnId) {
        // 查找专栏信息
        ColumnInfoDO column = columnDao.getById(columnId);
        if (column == null) {
            throw ExceptionUtil.of(StatusEnum.COLUMN_NOT_EXISTS, columnId);
        }
        return ColumnConvert.toDto(column);
    }

    @Override
    public ColumnArticleDO queryColumnArticle(long columnId, Integer section) {
        ColumnArticleDO article = columnDao.getColumnArticleId(columnId, section);
        if (article == null) {
            throw ExceptionUtil.of(StatusEnum.ARTICLE_NOT_EXISTS, section);
        }
        return article;
    }

    @Override
    public List<SimpleArticleDTO> queryColumnArticles(long columnId) {
        return columnDao.listColumnArticles(columnId);
    }

    private ColumnDTO buildColumnInfo(ColumnInfoDO info) {
        return buildColumnInfo(ColumnConvert.toDto(info));
    }

}
