package com.xiongsu.web.controller.category.rest;

import com.xiongsu.api.vo.article.dto.CategoryDTO;
import com.xiongsu.service.article.repository.entity.CategoryDO;
import com.xiongsu.service.article.service.CategoryService;
import com.xiongsu.web.global.vo.ResultVo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 类目的api
 */

@RestController
@RequestMapping("api/category")
public class CategoryRestController {

    @Resource
    private CategoryService categoryService;

    /**
     * 获取所有的分类标签
     */
    @GetMapping(path = "list/all")
    public ResultVo<List<CategoryDTO>> listall() {
        List<CategoryDTO> categoryDTOS = categoryService.loadAllCategories();
        return ResultVo.ok(categoryDTOS);
    }

}
