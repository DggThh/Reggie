package com.cy.reggie.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cy.reggie.entity.DishFlavor;

import java.util.List;

public interface DishFlavorService extends IService<DishFlavor> {
    //永久删除指定数据
    public int permanentlyDelete(List<DishFlavor> dishFlavorList);
}
