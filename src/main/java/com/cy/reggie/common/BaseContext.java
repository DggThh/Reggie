package com.cy.reggie.common;

//基于ThreadLocal封装的工具类，用于保存和获取当前登录账户的id
//该类作用范围是在每一个线程内
public class BaseContext {
    private static ThreadLocal<Long> longThreadLocal = new ThreadLocal();

    //存入id
    public static void setCurrentId(Long id) {
        longThreadLocal.set(id);
    }

    //获取id
    public static Long getCurrentId() {
        return longThreadLocal.get();
    }
}
