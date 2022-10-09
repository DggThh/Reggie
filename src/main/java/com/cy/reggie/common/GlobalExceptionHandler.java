package com.cy.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

//全局异常处理
//生命改类的异常处理类，并指定拦截的类上添加的注解
@ControllerAdvice(annotations = {RestController.class, Controller.class})
//处理完后需要返回 Json 数据
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {

    //指定处理的异常类型为 SQLIntegrityConstraintViolationException
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex) {
        log.info(ex.getMessage());
        //判断异常信息是否是有报 Duplicate entry ，即 用户名重复
        if (ex.getMessage().contains("Duplicate entry")) {
            String msg = "用户 " + ex.getMessage().split(" ")[2] + " 已存在";
            return R.error(msg);
        }
        return R.error("未知错误");
    }

    //处理分类下还有关联菜品或套餐的异常
    @ExceptionHandler(CustomException.class)
    public R<String> exceptionHandler(CustomException cex) {
        log.info(cex.getMessage());
        return R.error(cex.getMessage());
    }
}
