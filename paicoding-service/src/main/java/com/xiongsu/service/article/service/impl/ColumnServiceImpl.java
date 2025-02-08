package com.xiongsu.service.article.service.impl;

import com.xiongsu.api.exception.ExceptionUtil;
import com.xiongsu.api.vo.PageListVo;
import com.xiongsu.api.vo.PageParam;
import com.xiongsu.api.vo.article.dto.ColumnDTO;
import com.xiongsu.api.vo.article.dto.SimpleArticleDTO;
import com.xiongsu.api.vo.constants.StatusEnum;
import com.xiongsu.api.vo.user.dto.BaseUserInfoDTO;
import com.xiongsu.api.vo.user.dto.ColumnFootCountDTO;
import com.xiongsu.service.article.conveter.ColumnConvert;
import com.xiongsu.service.article.repository.dao.ArticleDao;
import com.xiongsu.service.article.repository.dao.ColumnArticleDao;
import com.xiongsu.service.article.repository.dao.ColumnDao;
import com.xiongsu.service.article.repository.entity.ColumnArticleDO;
import com.xiongsu.service.article.repository.entity.ColumnInfoDO;
import com.xiongsu.service.article.service.ColumnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ColumnServiceImpl implements ColumnService {


    @Autowired
    private ColumnDao columnDao;
    @Autowired
    private ArticleDao articleDao;

    @Autowired
    private ColumnArticleDao columnArticleDao;

    @Autowired
    private UserService userService;


    @Override
    public ColumnArticleDO getColumnArticleRelation(Long articleId) {
        return columnArticleDao.selectColumnArticleByArticleId(articleId);
    }

    /**
     * 专栏列表
     * @param pageParam
     * @return
     */
    @Override
    public PageListVo<ColumnDTO> listColumn(PageParam pageParam) {
        List<ColumnInfoDO> columnList = columnDao.listOnlineColumns(pageParam);
        List<ColumnDTO> result = columnList.stream().map(this::buildColumnInfo).collect(Collectors.toList());
        return PageListVo.newVo(result, pageParam.getPageSize());
    }

    @Override
    public ColumnArticleDO queryColumnArticle(long columnId, Integer order) {
        return null;
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
    public ColumnDTO queryColumnInfo(Long columnId) {
        return buildColumnInfo(queryBasicColumnInfo(columnId));
    }

    private ColumnDTO buildColumnInfo(ColumnInfoDO info) {
        return buildColumnInfo(ColumnConvert.toDto(info));
    }

    /**
     * 构建专栏详情信息
     *
     * @param dto
     * @return
     */
    private ColumnDTO buildColumnInfo(ColumnDTO dto) {
        // 补齐专栏对应的用户信息
        BaseUserInfoDTO user = userService.queryBasicUserInfo(dto.getAuthor());
        dto.setAuthorName(user.getUserName());
        dto.setAuthorAvatar(user.getPhoto());
        dto.setAuthorProfile(user.getProfile());

        // 统计计数
        ColumnFootCountDTO countDTO = new ColumnFootCountDTO();
        // 更新文章数
        countDTO.setArticleCount(columnDao.countColumnArticles(dto.getColumnId()));
        // 专栏阅读人数
        countDTO.setReadCount(columnDao.countColumnReadPeoples(dto.getColumnId()));
        // 总的章节数
        countDTO.setTotalNums(dto.getNums());
        dto.setCount(countDTO);
        return dto;
    }
    @Override
    public List<SimpleArticleDTO> queryColumnArticles(long columnId) {
        return columnDao.listColumnArticles(columnId);
    }

    @Override
    public Long getTutorialCount() {
        return this.columnDao.countColumnArticles();
    }
}
