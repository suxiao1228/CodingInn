package com.xiongsu.service.article.service.impl;


import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.xiongsu.api.enums.YesOrNoEnum;
import com.xiongsu.api.vo.article.dto.CategoryDTO;
import com.xiongsu.service.article.repository.dao.CategoryDao;
import com.xiongsu.service.article.repository.entity.CategoryDO;
import com.xiongsu.service.article.service.CategoryService;
import jakarta.annotation.PostConstruct;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 类目Service
 *
 * @author XuYifei
 * @date 2024-07-12
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    /**
     * 分类数一般不会特别多，如编程领域可以预期的分类将不会超过30，所以可以做一个全量的内存缓存
     * todo 后续可改为Guava -> Redis
     */
    private LoadingCache<Long, CategoryDTO> categoryCaches;

    private CategoryDao categoryDao;

    public CategoryServiceImpl(CategoryDao categoryDao) {
        this.categoryDao = categoryDao;
    }

    //在 Spring Bean 初始化时，创建一个本地缓存
    @PostConstruct
    public void init() {
        categoryCaches = CacheBuilder.newBuilder().maximumSize(300).build(new CacheLoader<Long, CategoryDTO>() {
            @Override
            public CategoryDTO load(@NotNull Long categoryId) throws Exception {
                CategoryDO category = categoryDao.getById(categoryId);
                if (category == null || category.getDeleted() == YesOrNoEnum.YES.getCode()) {
                    return CategoryDTO.EMPTY;
                }
                return new CategoryDTO(categoryId, category.getCategoryName(), category.getRank());
            }
        });
    }
    @Override
    public String queryCategoryName(Long categoryId) {
        return categoryCaches.getUnchecked(categoryId).getCategory();
    }

    @Override
    public List<CategoryDTO> loadAllCategories() {
        return List.of();
    }

    @Override
    public Long queryCategoryId(String category) {
        return 0L;
    }

    @Override
    public void refreshCache() {

    }
}
