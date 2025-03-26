package com.xiongsu.web.admin.rest;

import com.xiongsu.api.enums.user.UserAIStatEnum;
import com.xiongsu.api.vo.PageVo;
import com.xiongsu.api.vo.ResVo;
import com.xiongsu.api.vo.user.SearchZsxqUserReq;
import com.xiongsu.api.vo.user.ZsxqUserBatchOperateReq;
import com.xiongsu.api.vo.user.ZsxqUserPostReq;
import com.xiongsu.api.vo.user.dto.ZsxqUserInfoDTO;
import com.xiongsu.core.permission.Permission;
import com.xiongsu.core.permission.UserRole;
import com.xiongsu.service.user.service.ZsxqWhiteListService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 作者白名单服务
 */
@RestController
@Tag(name = "用户白名单管理控制器", description = "白名单")
@Permission(role = UserRole.ADMIN)
@RequestMapping(path = {"api/admin/zsxq/whitelist"})
public class ZsxqWhiteListController {
    @Resource
    private ZsxqWhiteListService zsxqWhiteListService;

    @Operation(summary = "获取白名单用户列表")
    @GetMapping(path = "")
    public ResVo<PageVo<ZsxqUserInfoDTO>> list(@RequestBody SearchZsxqUserReq req) {
        PageVo<ZsxqUserInfoDTO> articleDTOPageVo = zsxqWhiteListService.getList(req);
        return ResVo.ok(articleDTOPageVo);
    }

    // 改变用户状态， 审核通过
    @Operation(summary = "改变用户状态")
    @GetMapping(path = "operate")
    public ResVo<String> operate(@RequestParam(name = "id") Long id,
                                 @RequestParam(name = "status") Integer status) {
        UserAIStatEnum operate = UserAIStatEnum.fromCode(status);
        zsxqWhiteListService.operate(id, operate);
        return ResVo.ok("ok");
    }

    @Permission(role = UserRole.ADMIN)
    @GetMapping(path = "reset")
    public ResVo<String> reset(@RequestParam(name = "authorId") Integer authorId) {
        zsxqWhiteListService.reset(authorId);
        return ResVo.ok("ok");
    }

    // 批量审核通过
    @Operation(summary = "批量审核通过")
    @PostMapping(path = "batchOperate")
    public ResVo<String> batchOperate(@RequestBody ZsxqUserBatchOperateReq req) {
        UserAIStatEnum operate = UserAIStatEnum.fromCode(req.getStatus());
        zsxqWhiteListService.batchOperate(req.getIds(), operate);
        return ResVo.ok("ok");
    }

    @PostMapping(path = "save")
    public ResVo<String> save(@RequestBody ZsxqUserPostReq req) {
        zsxqWhiteListService.update(req);
        return ResVo.ok("ok");
    }
}
