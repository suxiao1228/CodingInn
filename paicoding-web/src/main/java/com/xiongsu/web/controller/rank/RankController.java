package com.xiongsu.web.controller.rank;

import com.xiongsu.api.enums.rank.ActivityRankTimeEnum;
import com.xiongsu.api.vo.ResVo;
import com.xiongsu.api.vo.rank.dto.RankInfoDTO;
import com.xiongsu.api.vo.rank.dto.RankItemDTO;
import com.xiongsu.service.rank.service.UserActivityRankService;
import jakarta.annotation.Resource;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * 排行榜
 */
@Controller
public class RankController {
    @Resource
    private UserActivityRankService userActivityRankService;

    @RequestMapping(path = "/rank/{time}")
    public String rank(@PathVariable(value = "time") String time, Model model) {
        ActivityRankTimeEnum rankTime = ActivityRankTimeEnum.nameOf(time);
        if (rankTime == null) {
            rankTime = ActivityRankTimeEnum.MONTH;
        }
        List<RankItemDTO> list = userActivityRankService.queryRankList(rankTime, 30);
        RankInfoDTO info = new RankInfoDTO();
        info.setItems(list);
        info.setTime(rankTime);
        ResVo<RankInfoDTO> vo = ResVo.ok(info);
        model.addAttribute("vo", vo);
        return "views/rank/index";
    }

}
