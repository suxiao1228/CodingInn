package com.xiongsu.service.article.service;


import com.xiongsu.api.vo.article.dto.CategoryDTO;

import java.util.List;

/**
 * 标签
 */
public interface CategoryService {
    /**
     * 查询类目名
     *
     * @param categoryId
     * @return
     */
    String queryCategoryName(Long categoryId);


    /**
     * 查询所有的分离
     *
     * @return
     */
    List<CategoryDTO> loadAllCategories();

    /**
     * 查询类目id
     *
     * @param category
     * @return
     */
    Long queryCategoryId(String category);


    /**
     * 刷新缓存
     */
    public void refreshCache();
}
