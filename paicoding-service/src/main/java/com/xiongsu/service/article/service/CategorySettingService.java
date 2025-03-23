package com.xiongsu.service.article.service;

import com.xiongsu.api.vo.PageVo;
import com.xiongsu.api.vo.article.CategoryReq;
import com.xiongsu.api.vo.article.SearchCategoryReq;
import com.xiongsu.api.vo.article.dto.CategoryDTO;

/**
 * 分类后台接口
 */
public interface CategorySettingService {

    void saveCategory(CategoryReq categoryReq);

    void deleteCategory(Integer categoryId);

    void operateCategory(Integer categoryId, Integer pushStatus);

    /**
     * 获取category列表
     *
     * @param params
     * @return
     */
    PageVo<CategoryDTO> getCategoryList(SearchCategoryReq params);
}
