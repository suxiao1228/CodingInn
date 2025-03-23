package com.xiongsu.web.admin.rest;


import com.xiongsu.api.enums.PushStatusEnum;
import com.xiongsu.api.vo.PageVo;
import com.xiongsu.api.vo.ResVo;
import com.xiongsu.api.vo.article.CategoryReq;
import com.xiongsu.api.vo.article.SearchCategoryReq;
import com.xiongsu.api.vo.article.dto.CategoryDTO;
import com.xiongsu.api.vo.constants.StatusEnum;
import com.xiongsu.core.permission.Permission;
import com.xiongsu.core.permission.UserRole;
import com.xiongsu.service.article.service.CategorySettingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.elasticsearch.search.SearchService;
import org.springframework.web.bind.annotation.*;

/**
 * 分类后台
 */
@RestController
@Permission(role = UserRole.ADMIN)
@Tag(name = "文章类目管理控制器", description = "类目管理")
@RequestMapping(path = {"api/admin/category/", "admin/category/"})

public class CategorySettingRestController {
    @Resource
    private CategorySettingService categorySettingService;

    /**
     * 保存分类
     * @param req
     * @return
     */
    @Permission(role = UserRole.ADMIN)
    @PostMapping(path = "save")
    public ResVo<String> save(@RequestBody CategoryReq req) {
        categorySettingService.saveCategory(req);
        return ResVo.ok("ok");
    }

    /**
     * 删除分类
     */
    @Permission(role = UserRole.ADMIN)
    @GetMapping(path = "delete")
    public ResVo<String> delete(@RequestParam(name = "categoryId") Integer categoryId) {
        categorySettingService.deleteCategory(categoryId);
        return ResVo.ok("ok");
    }

    /**
     * 获取分类信息
     */
    @Permission(role = UserRole.ADMIN)
    @GetMapping(path = "operate")
    public ResVo<String> operate(@RequestParam(name = "categoryId") Integer categoryId,
                                 @RequestParam(name = "pushStatus") Integer pushStatus) {
        if (pushStatus != PushStatusEnum.OFFLINE.getCode() && pushStatus != PushStatusEnum.ONLINE.getCode()) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS);
        }
        categorySettingService.operateCategory(categoryId, pushStatus);
        return ResVo.ok("ok");
    }

    @PostMapping(path = "list")
    public ResVo<PageVo<CategoryDTO>> list(@RequestBody SearchCategoryReq req) {
        PageVo<CategoryDTO> categoryDTOPageVo = categorySettingService.getCategoryList(req);
        return ResVo.ok(categoryDTOPageVo);
    }
}
