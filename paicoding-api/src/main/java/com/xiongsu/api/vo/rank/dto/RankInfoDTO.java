package com.xiongsu.api.vo.rank.dto;

import com.xiongsu.api.enums.rank.ActivityRankTimeEnum;
import lombok.Data;

import java.util.List;

/**
 * 排行榜信息
 *
 * @author YiHui
 * @date 2023/8/19
 */
@Data
public class RankInfoDTO {
    private ActivityRankTimeEnum time;
    private List<RankItemDTO> items;
}
