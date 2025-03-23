package com.xiongsu.service.article.service.impl;

import com.xiongsu.api.vo.PageVo;
import com.xiongsu.api.vo.article.CategoryReq;
import com.xiongsu.api.vo.article.SearchCategoryReq;
import com.xiongsu.api.vo.article.dto.CategoryDTO;
import com.xiongsu.core.util.NumUtil;
import com.xiongsu.service.article.conveter.CategoryStructMapper;
import com.xiongsu.service.article.repository.dao.CategoryDao;
import com.xiongsu.service.article.repository.entity.CategoryDO;
import com.xiongsu.service.article.repository.params.SearchArticleParams;
import com.xiongsu.service.article.repository.params.SearchCategoryParams;
import com.xiongsu.service.article.service.CategoryService;
import com.xiongsu.service.article.service.CategorySettingService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class CategorySettingServiceImpl implements CategorySettingService {

    @Resource
    private CategoryDao categoryDao;

    @Resource
    private CategoryService categoryService;

    @Override
    public void saveCategory(CategoryReq categoryReq) {
        CategoryDO categoryDO = CategoryStructMapper.INSTANCE.toDO(categoryReq);
        if (NumUtil.nullOrZero(categoryReq.getCategoryId())) {
            categoryDao.save(categoryDO);
        } else {
            categoryDO.setId(categoryReq.getCategoryId());
            categoryDao.updateById(categoryDO);
        }
        categoryService.refreshCache();
    }


    @Override
    public void deleteCategory(Integer categoryId) {
        CategoryDO categoryDO = categoryDao.getById(categoryId);
        if (categoryDO != null) {
            categoryDao.removeById(categoryDO);
        }
        categoryService.refreshCache();
    }

    @Override
    public void operateCategory(Integer categoryId, Integer pushStatus) {
        CategoryDO categoryDO = categoryDao.getById(categoryId);
        if (categoryDO != null) {
            categoryDO.setStatus(pushStatus);
            categoryDao.updateById(categoryDO);
        }
        categoryService.refreshCache();
    }

    @Override
    public PageVo<CategoryDTO> getCategoryList(SearchCategoryReq req) {
        //转换
        SearchCategoryParams params = CategoryStructMapper.INSTANCE.toSearchParams(req);
        //查询
        List<CategoryDTO> categoryDTOS = categoryDao.listCategory(params);
        Long totalCount = categoryDao.countCategory(params);
        return PageVo.build(categoryDTOS, params.getPageSize(), params.getPageNum(), totalCount);
    }
}
