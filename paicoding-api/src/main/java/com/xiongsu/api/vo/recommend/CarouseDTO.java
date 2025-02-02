package com.xiongsu.api.vo.recommend;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author YiHui
 * @date 2022/9/7
 */
@Data
@Accessors(chain = true)
public class CarouseDTO implements Serializable {

    private static final long serialVersionUID = 1048555496974144842L;
    /**
     * 说明
     */
    private String name;
    /**
     * 图片地址
     */
    private String imgUrl;
    /**
     * 跳转地址
     */
    private String actionUrl;
}
