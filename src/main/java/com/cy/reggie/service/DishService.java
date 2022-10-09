package com.cy.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cy.reggie.dto.DishDto;
import com.cy.reggie.entity.Dish;
import org.springframework.stereotype.Service;

public interface DishService  extends IService<Dish> {
    //新增菜品时，要将口味数据插入到口味的表中
    public void saveWithFlavor(DishDto dishDto);

    //根据id查询菜品信息和该菜品口味信息
    public DishDto getByIdWithFlavor(Long id);

    //改菜品信息
    public void updateWithFlavor(DishDto dishDto);

    //菜品与菜品口味表信息的删除
    public  void deleteWithFlavor(Long[] ids);
}
