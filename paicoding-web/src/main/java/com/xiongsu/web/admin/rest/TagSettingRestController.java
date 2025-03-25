package com.xiongsu.web.admin.rest;

import cn.hutool.db.Page;
import com.xiongsu.api.enums.PushStatusEnum;
import com.xiongsu.api.vo.PageVo;
import com.xiongsu.api.vo.ResVo;
import com.xiongsu.api.vo.article.SearchTagReq;
import com.xiongsu.api.vo.article.TagReq;
import com.xiongsu.api.vo.article.dto.TagDTO;
import com.xiongsu.api.vo.constants.StatusEnum;
import com.xiongsu.core.permission.Permission;
import com.xiongsu.core.permission.UserRole;
import com.xiongsu.service.article.service.TagSettingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 标签后台
 */
@RestController
@Permission(role = UserRole.LOGIN)
@Tag(name = "文章标签管理控制器", description = "标签管理")
@RequestMapping(path = {"api/admin/tag/", "admin/tag/"})
public class TagSettingRestController {

    @Resource
    private TagSettingService tagSettingService;

    @Permission(role = UserRole.ADMIN)
    @GetMapping(path = "save")
    public ResVo<String> save(@RequestBody TagReq req) {
        tagSettingService.saveTag(req);
        return ResVo.ok("ok");
    }

    @Permission(role = UserRole.ADMIN)
    @GetMapping(path = "delete")
    public ResVo<String> delete(@RequestParam(name = "tagId") Integer tagId) {
        tagSettingService.deleteTag(tagId);
        return ResVo.ok("ok");
    }

    @Permission(role = UserRole.ADMIN)
    @GetMapping(path = "operate")
    public ResVo<String> operate(@RequestParam(name = "tagId") Integer tagId,
                                 @RequestParam(name = "pushStatus") Integer pushStatus) {
        if (pushStatus != PushStatusEnum.OFFLINE.getCode() && pushStatus!= PushStatusEnum.ONLINE.getCode()) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS);
        }
        tagSettingService.operateTag(tagId, pushStatus);
        return ResVo.ok("ok");
    }

    @PostMapping(path = "list")
    public ResVo<PageVo<TagDTO>> list(@RequestBody SearchTagReq req) {
        PageVo<TagDTO> tagDTOPageVo = tagSettingService.getTagList(req);
        return ResVo.ok(tagDTOPageVo);
    }
}
