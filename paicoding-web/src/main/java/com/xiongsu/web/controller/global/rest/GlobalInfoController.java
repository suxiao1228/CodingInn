package com.xiongsu.web.controller.global.rest;

import com.xiongsu.web.global.vo.ResultVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.xml.transform.Result;

@RestController
@RequestMapping("api/global")
public class GlobalInfoController {
    /**
     * 用于单独提供一个接口，用于前端获取全局信息
     */
    @GetMapping("info")
    public ResultVo<String> getGlobalInfo() {
        return ResultVo.ok("ok");
    }
}
