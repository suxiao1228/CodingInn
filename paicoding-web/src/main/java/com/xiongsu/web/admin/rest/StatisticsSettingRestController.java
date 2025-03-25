package com.xiongsu.web.admin.rest;

import com.xiongsu.api.vo.ResVo;
import com.xiongsu.api.vo.statistics.dto.StatisticsCountDTO;
import com.xiongsu.api.vo.statistics.dto.StatisticsDayDTO;
import com.xiongsu.core.permission.Permission;
import com.xiongsu.core.permission.UserRole;
import com.xiongsu.service.statistics.service.StatisticsSettingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据统计后台
 */
@RestController
@Permission(role = UserRole.ADMIN)
@Tag(name = "全栈统计分析控制器", description = "统计分析")
@RequestMapping(path = {"api/admin/statistics/", "admin/statistics/"})
public class StatisticsSettingRestController {
    @Resource
    private StatisticsSettingService statisticsSettingService;

    static final Integer DEFAULT_DAY = 7;

    /**
     * 查询统计总数
     * @return
     */
    @GetMapping(path = "queryTotal")
    public ResVo<StatisticsCountDTO> queryTotal() {
        StatisticsCountDTO statisticsCountDTO = statisticsSettingService.getStatisticsCount();
        return ResVo.ok(statisticsCountDTO);
    }

    @ResponseBody
    @GetMapping(path = "pvUvDayList")
    public ResVo<List<StatisticsDayDTO>> pvUvDayList(@RequestParam(name = "day", required = false) Integer day) {
        day = (day == null || day == 0) ? DEFAULT_DAY : day;
        List<StatisticsDayDTO> pvDayList = statisticsSettingService.getPvUvDayList(day);
        return ResVo.ok(pvDayList);
    }
}
