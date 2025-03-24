package com.xiongsu.web.admin.rest;

import com.xiongsu.api.vo.PageVo;
import com.xiongsu.api.vo.ResVo;
import com.xiongsu.api.vo.config.GlobalConfigReq;
import com.xiongsu.api.vo.config.SearchGlobalConfigReq;
import com.xiongsu.api.vo.config.dto.GlobalConfigDTO;
import com.xiongsu.core.permission.Permission;
import com.xiongsu.core.permission.UserRole;
import com.xiongsu.service.config.repository.entity.GlobalConfigDO;
import com.xiongsu.service.config.service.GlobalConfigService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * 标签后台
 */
@RestController
@Permission(role = UserRole.ADMIN)
@Tag(name = "全局配置管理控制器", description = "全局配置")
@RequestMapping(path = {"api/admin/global/config/", "admin/global/config/"})
public class GlobalConfigRestController {
    @Resource
    private GlobalConfigService globalConfigService;

    /**
     * 保存标签
     * @param req
     * @return
     */
    @Permission(role = UserRole.ADMIN)
    @PostMapping(path = "save")
    public ResVo<String> save(@RequestBody GlobalConfigReq req) {
        globalConfigService.save(req);
        return ResVo.ok("ok");
    }

    @Permission(role = UserRole.ADMIN)
    @GetMapping(path = "delete")
    public ResVo<String> delete(@RequestParam(name = "id") Long id) {
        globalConfigService.delete(id);
        return ResVo.ok("ok");
    }

    @Permission(role = UserRole.ADMIN)
    @PostMapping("list")
    public ResVo<PageVo<GlobalConfigDTO>> list(@RequestBody SearchGlobalConfigReq req) {
        PageVo<GlobalConfigDTO> page = globalConfigService.getList(req);
        return ResVo.ok(page);
    }
}
