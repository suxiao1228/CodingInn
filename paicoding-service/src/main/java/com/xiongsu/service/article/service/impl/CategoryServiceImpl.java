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

import java.util.ArrayList;
import java.util.Comparator;
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
    //🔹 1. LoadingCache 作用
    //LoadingCache<K, V> 是 Guava Cache 的核心接口之一，支持 自动加载 和 过期清理，用于存储键值对（K -> V）。
    //
    //在你的代码中：
    //
    //Long 作为 键（categoryId）。
    //CategoryDTO 作为 值（分类数据对象）。
    //categoryCaches 充当 缓存容器，存储 CategoryDTO 以减少数据库查询，提高访问速度。
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

    /**
     * 查询所有的分类
     * @param categoryId
     * @return
     */
    @Override
    public String queryCategoryName(Long categoryId) {
        if (categoryCaches.size() <= 5) {
            refreshCache();
        }
        List<CategoryDTO> list = new ArrayList<>(categoryCaches.asMap().values());
        list.removeIf(s -> s.getCategoryId() <= 0);
        list.sort(Comparator.comparingInt(CategoryDTO::getRank));
        return list.toString();
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
