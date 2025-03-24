package com.xiongsu.web.admin.rest;

import com.xiongsu.api.enums.PushStatusEnum;
import com.xiongsu.api.vo.PageVo;
import com.xiongsu.api.vo.ResVo;
import com.xiongsu.api.vo.banner.ConfigReq;
import com.xiongsu.api.vo.banner.SearchConfigReq;
import com.xiongsu.api.vo.banner.dto.ConfigDTO;
import com.xiongsu.api.vo.constants.StatusEnum;
import com.xiongsu.core.permission.Permission;
import com.xiongsu.core.permission.UserRole;
import com.xiongsu.service.config.service.ConfigSettingService;
import com.xiongsu.service.config.service.impl.ConfigSettingServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.models.security.SecurityScheme;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * Banner后台
 */
@RestController
@Permission(role = UserRole.LOGIN)
@Tag(name = "后台运营配置管理控制器", description = "配置管理")
@RequestMapping(path = {"api/admin/config/", "admin/config/"})
public class ConfigSettingrRestController {
    @Resource
    private ConfigSettingServiceImpl configSettingService;

    /**
     * 保存
     * @param req
     * @return
     */
    @Permission(role = UserRole.ADMIN)
    @PostMapping(path = "save")
    public ResVo<String> save(@RequestBody ConfigReq req) {
        configSettingService.saveConfig(req);
        return ResVo.ok("ok");
    }

    /**
     * 删除
     * @param configId
     * @return
     */
    @Permission(role = UserRole.ADMIN)
    @GetMapping(path = "delete")
    public ResVo<String> delete(@RequestParam(name = "configId")Integer configId) {
        configSettingService.deleteConfig(configId);
        return ResVo.ok("ok");
    }

    /**
     * 操作
     * @param configId
     * @param pushStatus
     * @return
     */
    @Permission(role = UserRole.ADMIN)
    @GetMapping(path = "operate")
    public ResVo<String> operate(@RequestParam(name = "configId") Integer configId,
                                 @RequestParam(name = "pushStatus") Integer pushStatus) {
        if (pushStatus != PushStatusEnum.OFFLINE.getCode() && pushStatus != PushStatusEnum.ONLINE.getCode()) {
            return ResVo.fail(StatusEnum.ILLEGAL_ARGUMENTS);
        }
        configSettingService.operateConfig(configId, pushStatus);
        return ResVo.ok("ok");
    }

    /**
     *  获取配置列表
     */
    @PostMapping(path = "list")
    public ResVo<PageVo<ConfigDTO>> list(@RequestBody SearchConfigReq req) {
        PageVo<ConfigDTO> bannerDTOPageVo = configSettingService.getConfigList(req);
        return ResVo.ok(bannerDTOPageVo);
    }
}
