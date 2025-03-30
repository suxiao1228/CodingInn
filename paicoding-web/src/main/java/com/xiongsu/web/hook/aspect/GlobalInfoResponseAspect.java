package com.xiongsu.web.hook.aspect;

import com.xiongsu.web.global.GlobalInitService;
import com.xiongsu.web.global.vo.ResultVo;
import jakarta.annotation.Resource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

//这个类是一个 Spring AOP 切面（Aspect），用于拦截全局的 Controller 返回值，并给 ResultVo 结果对象添加全局属性。
@Aspect
@Component
public class GlobalInfoResponseAspect {

    @Resource
    private GlobalInitService globalInitService;

    @Pointcut("execution(public com.xiongsu.web.global.vo.ResultVo com.xiongsu.web..*.*(..))")
    public void controllerMethods() {}

    @Around("controllerMethods()")
    public Object modifyGlobalResponse(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed(); // 继续执行原方法

        if (result instanceof ResultVo) {
            ((ResultVo<?>) result).setGlobal(globalInitService.globalAttr());
        }

        return result;
    }
}
