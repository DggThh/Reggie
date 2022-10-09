package com.cy.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cy.reggie.dto.DishDto;
import com.cy.reggie.entity.Dish;
import com.cy.reggie.entity.DishFlavor;
import com.cy.reggie.mapper.DishMapper;
import com.cy.reggie.service.DishFlavorService;
import com.cy.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.beans.Transient;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;

    //新增菜品时，要将口味数据插入到口味的表中
    @Override
    @Transactional//因为该操作关系到多张表，所以要加入事务控制,保证数据的一致性
    public void saveWithFlavor(DishDto dishDto) {
        //将菜品信息存入菜品表，多余的信息会被无视
        this.save(dishDto);

        //获取保存的菜品的id
        Long dishId = dishDto.getId();

        //获取菜品口味的 List
        List<DishFlavor> flavors = dishDto.getFlavors();

        //利用流遍历 flavors ，并设置每个元素中的 DishId，最后将流再转换成 List
        flavors.stream().map((item) -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());

        //将剩余的口味信息批量保存到口味表
        dishFlavorService.saveBatch(flavors);
    }

    //根据id查询菜品信息和该菜品口味信息
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //获取菜品的普通属性
        Dish dish = this.getById(id);
        DishDto dishDto = new DishDto();
        //先将菜品的普通属性拷贝到 dishDto 中
        BeanUtils.copyProperties(dish, dishDto);
        //创建条件构造器
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper();
        //利用菜品的id，查询菜品口味的信息
        queryWrapper.eq(DishFlavor::getDishId, id);
        List<DishFlavor> list = dishFlavorService.list(queryWrapper);
        //将菜品的口味信息补上
        dishDto.setFlavors(list);
        //返回
        return dishDto;
    }

    //改菜品信息
    @Override
    @Transactional//因为该操作关系到多张表，所以要加入事务控制,保证数据的一致性
    public void updateWithFlavor(DishDto dishDto) {
        //更新菜品表信息
        this.updateById(dishDto);
        //获取保存的菜品的id
        Long dishId = dishDto.getId();
        //清理当前的口味信息表中的数据
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(DishFlavor::getDishId, dishId);
        List<DishFlavor> list = dishFlavorService.list(queryWrapper);
        dishFlavorService.permanentlyDelete(list);
        //添加当前的口味信息数据
        //获取菜品口味的 List
        List<DishFlavor> flavors = dishDto.getFlavors();
        //利用流遍历 flavors ，并设置每个元素中的 DishId，最后将流再转换成 List
        flavors.stream().map((item) -> {
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());
        //将剩余的口味信息批量保存到口味表
        dishFlavorService.saveBatch(flavors);
    }

    //菜品与菜品口味表信息的删除
    @Override
    @Transactional
    public  void deleteWithFlavor(Long[] ids) {
        List<Long> idList =Arrays.asList(ids);
        this.removeByIds(idList);
        LambdaQueryWrapper<DishFlavor> queryWrapper=new LambdaQueryWrapper();
        queryWrapper.in(ids != null, DishFlavor::getDishId, ids);
        dishFlavorService.remove(queryWrapper);
    }
}
