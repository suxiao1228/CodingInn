package com.xiongsu.service.user.repository.params;

import com.xiongsu.api.vo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SearchZsxqWhiteParams extends PageParam {

    /**
     * 审核状态
     */
    private Integer status;

    /**
     * 星球编号
     */
    private String starNumber;

    /**
     * 登录用户名
     */
    private String name;

    /**
     * 用户编号
     */
    private String userCode;

}