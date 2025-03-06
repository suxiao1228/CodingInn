package com.xiongsu.web.global;

import com.xiongsu.api.vo.PageParam;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 全局属性配置
 *
 * @author XuYifei
 * @date 2024-07-12
 */
public class BaseViewController {
    @Resource
    protected GlobalInitService globalInitService;

    public PageParam buildPageParam(Long page, Long size) {
        if (page <= 0) {
            page = PageParam.DEFAULT_PAGE_NUM;
        }
        if (size == null || size > PageParam.DEFAULT_PAGE_SIZE) {
            size = PageParam.DEFAULT_PAGE_SIZE;
        }
        return PageParam.newPageInstance(page, size);
    }
}
