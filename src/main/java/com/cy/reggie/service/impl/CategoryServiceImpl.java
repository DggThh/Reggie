package com.cy.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cy.reggie.common.CustomException;
import com.cy.reggie.entity.Category;
import com.cy.reggie.entity.Dish;
import com.cy.reggie.entity.Setmeal;
import com.cy.reggie.mapper.CategoryMapper;
import com.cy.reggie.service.CategoryService;
import com.cy.reggie.service.DishService;
import com.cy.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    //要利用 DishService 查询是否有关联的菜品
    @Autowired
    private DishService dishService;

    //要利用 SetmealService 查询是否有关联的菜品
    @Autowired
    private SetmealService setmealService;

    //根据id删除分类，在删除前进行判断
    @Override
    public void remove(Long id) {
        //查询 菜品 表中有几个 CategoryId 是指定 id 的
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper();
        dishLambdaQueryWrapper.eq(Dish::getCategoryId, id);
        int dishCount = dishService.count(dishLambdaQueryWrapper);
        //查询当前分类是否有关联的菜品，如果还有关联，就抛出异常
        if (dishCount > 0)
            throw new CustomException("当前分类下还有关联的菜品，无法删除");

        //查询 套餐 表中有几个 CategoryId 是指定 id 的
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId, id);
        int setmealCount = setmealService.count(setmealLambdaQueryWrapper);
        //查询当前分类是否有关联的套餐，如果还有关联，就抛出异常
        if (setmealCount > 0)
            throw new CustomException("当前分类下还有关联的套餐，无法删除");

        //都没有关联就正常删除分类
        super.removeById(id);
    }
}
