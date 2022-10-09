package com.cy.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cy.reggie.dto.DishDto;
import com.cy.reggie.dto.SetmealDto;
import com.cy.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    //保存套餐的基本信息与套餐中菜品的信息
    public void saveWithDish(SetmealDto setmealDto);

    //删除套餐时，要将套餐菜品表中关联的菜品也进行
    public void removeWithDish(List ids);

    //根据id查询套餐的菜品信息
    public SetmealDto getByIdWithDish(Long id);

    //改菜品信息
    public void updateWithDish(SetmealDto setmealDto);

    //删除
}
