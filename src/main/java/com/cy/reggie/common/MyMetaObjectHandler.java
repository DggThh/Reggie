package com.cy.reggie.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

//自定义的元数据处理器
@Component//实例化 Bean 让 Spring 容器进行管理
@Slf4j
public class MyMetaObjectHandler implements MetaObjectHandler {
    //插入时执行的方法
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info("insertFill...  : " + metaObject.toString());
        //创建时间设置为当前时间
        metaObject.setValue("createTime", LocalDateTime.now());

        //更新时间
        metaObject.setValue("updateTime", LocalDateTime.now());

        //创建对象
        metaObject.setValue("createUser", BaseContext.getCurrentId());

        //更新人
        metaObject.setValue("updateUser", BaseContext.getCurrentId());
    }

    //更新时执行的方法
    @Override
    public void updateFill(MetaObject metaObject) {
        log.info("updateFill...  : " + metaObject.toString());
        //更新时间
        metaObject.setValue("updateTime", LocalDateTime.now());

        //更新人
        metaObject.setValue("updateUser", BaseContext.getCurrentId());
    }
}
