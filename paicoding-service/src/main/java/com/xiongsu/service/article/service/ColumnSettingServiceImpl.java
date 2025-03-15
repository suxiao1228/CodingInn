package com.xiongsu.service.article.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xiongsu.api.vo.PageVo;
import com.xiongsu.api.vo.article.*;
import com.xiongsu.api.vo.article.dto.ColumnArticleDTO;
import com.xiongsu.api.vo.article.dto.ColumnDTO;
import com.xiongsu.api.vo.article.dto.SimpleColumnDTO;
import com.xiongsu.service.article.repository.dao.ArticleDao;
import com.xiongsu.service.article.repository.dao.ColumnArticleDao;
import com.xiongsu.service.article.repository.dao.ColumnDao;
import com.xiongsu.service.article.repository.entity.ColumnArticleDO;
import com.xiongsu.service.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class ColumnSettingServiceImpl implements ColumnSettingService{

    @Autowired
    private UserService userService;

    @Autowired
    private ColumnArticleDao columnArticleDao;

    @Autowired
    private ColumnDao columnDao;

    @Autowired
    private ArticleDao articleDao;

    @Autowired
    private ColumnStructMapper columnStructMapper;

    /**
     * 将文章保存到对应的专栏中
     * @param articleId
     * @param columnId
     */
    @Override
    public void saveColumnArticle(Long articleId, Long columnId) {
        //转换参数
        // 插入的时候，需要判断是否已经存在
        ColumnArticleDO exist = columnArticleDao.getOne(Wrappers.lambdaQuery()
                .eq(ColumnArticleDO::getArticleId, articleId));
        if (exist != null) {
            if (!Objects.equals(columnId, exist.getColumnId())) {
                //更新
                exist.setColumnId(columnId);
                columnArticleDao.updateById(exist);
            }
        } else {
            // 将文章保存到专栏中， 章节序号+1
            ColumnArticleDO columnArticleDO = new ColumnArticleDO();
            columnArticleDO.setColumnId(columnId);
            columnArticleDO.setArticleId(articleId);
            // section 自增+1
            Integer maxSection = columnArticleDao.selectMaxSection(columnId);
            columnArticleDO.setSection(maxSection+1);
            columnArticleDao.save(columnArticleDO);
        }

    }

    @Override
    public void saveColumn(ColumnReq columnReq) {

    }

    @Override
    public void saveColumnArticle(ColumnArticleReq req) {

    }

    @Override
    public void deleteColumn(Long columnId) {

    }

    @Override
    public void deleteColumnArticle(Long id) {

    }

    @Override
    public List<SimpleColumnDTO> listSimpleColumnBySearchKey(String key) {
        return List.of();
    }

    @Override
    public PageVo<ColumnDTO> getColumnList(SearchColumnReq req) {
        return null;
    }

    @Override
    public PageVo<ColumnArticleDTO> getColumnArticleList(SearchColumnArticleReq req) {
        return null;
    }

    @Override
    public void sortColumnArticleApi(SortColumnArticleReq req) {

    }

    @Override
    public void sortColumnArticleByIDApi(SortColumnArticleByIDReq req) {

    }
}
