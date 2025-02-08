package com.xiongsu.service.article.service.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.xiongsu.api.enums.YesOrNoEnum;
import com.xiongsu.api.vo.article.dto.CategoryDTO;
import com.xiongsu.service.article.conveter.ArticleConverter;
import com.xiongsu.service.article.repository.dao.CategoryDao;
import com.xiongsu.service.article.repository.entity.CategoryDO;
import com.xiongsu.service.article.service.CategoryService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    /**
     * 分类数一般不会特别多，如编程领域可以预期的分类将不会超过30，所以可以做一个全量的内存缓存
     * @param categoryId
     * @return
     */

    private LoadingCache<Long, CategoryDTO> categoryCaches;

    private CategoryDao categoryDao;

    public CategoryServiceImpl(CategoryDao categoryDao) {
        this.categoryDao = categoryDao;
    }

    //缓存的键是 Long 类型的 categoryId
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
     * 查询类目名
     * @param categoryId
     * @return
     */
    @Override
    public String queryCategoryName(Long categoryId) {
        return categoryCaches.getUnchecked(categoryId).getCategory();
    }

    /**
     * 查询所有的分类
     * @return
     */
    @Override
    public List<CategoryDTO> loadAllCategories() {
        if (categoryCaches.size() <= 5) {
            refreshCache();
        }
        List<CategoryDTO> list = new ArrayList<>(categoryCaches.asMap().values());
        list.removeIf(s -> s.getCategoryId() <= 0);
        list.sort(Comparator.comparingInt(CategoryDTO::getRank));
        return list;
    }


    //根据给定的分类名称查询缓存中对应的分类 ID，快速响应分类查询请求。
    @Override
    public Long queryCategoryId(String category) {
        return categoryCaches.asMap().values().stream()
                .filter(s -> s.getCategory().equalsIgnoreCase(category))
                .findFirst().map(CategoryDTO::getCategoryId).orElse(null);
    }

    /**
     * 刷新缓存
     * @return
     */
    //负责从数据库中获取最新的分类数据，并刷新缓存，使缓存内容保持最新。
    @Override
    public void refreshCache() {
        List<CategoryDO> list = categoryDao.listAllCategoriesFromDb();
        categoryCaches.invalidateAll();
        categoryCaches.cleanUp();
        list.forEach(s -> categoryCaches.put(s.getId(), ArticleConverter.toDto(s)));
    }
}
