package com.cy.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cy.reggie.common.CustomException;
import com.cy.reggie.dto.DishDto;
import com.cy.reggie.dto.SetmealDto;
import com.cy.reggie.entity.Dish;
import com.cy.reggie.entity.DishFlavor;
import com.cy.reggie.entity.Setmeal;
import com.cy.reggie.entity.SetmealDish;
import com.cy.reggie.mapper.SetmealMapper;
import com.cy.reggie.service.SetmealDishService;
import com.cy.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    private SetmealDishService setmealDishService;

    //保存套餐的基本信息与套餐中菜品的信息
    @Override
    public void saveWithDish(SetmealDto setmealDto) {
        //保存套餐的基本信息
        this.save(setmealDto);

        log.info("1111111111111111111111111111111111111111111111111111111111111");

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        log.info("22222222222222222222222222222222222222222222222222222222222222");
        setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());
        log.info("3333333333333333333333333333333333333333333333333333333333333333");
        //保存套餐和菜品的关联信息
        setmealDishService.saveBatch(setmealDishes);
    }

    //删除套餐时，要将套餐菜品表中关联的菜品也进行
    @Override
    @Transactional
    public void removeWithDish(List ids) {
        //查询套餐是否处于停售状态（只能删除已停售的套餐）
        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper();
        queryWrapper.in(Setmeal::getId,ids);
        queryWrapper.eq(Setmeal::getStatus,1);
        int count = this.count(queryWrapper);
        //若不能删除，就抛出业务异常
        if(count>0)
            throw new CustomException("有套餐正在售卖中，请先停售，再尝试删除");

        //可删除时，先删除套餐表中的数据—— Setmeal表
        this.removeByIds(ids);

        //删除关系表中的数据—— Setmeal-Dish表
        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper=new LambdaQueryWrapper();
        setmealDishLambdaQueryWrapper.in(SetmealDish::getSetmealId,ids);
        setmealDishService.remove(setmealDishLambdaQueryWrapper);
    }

    //根据id查询套餐的菜品信息
    @Override
    public SetmealDto getByIdWithDish(Long id) {
        //获取套餐的普通属性
        Setmeal setmeal = this.getById(id);
        SetmealDto setmealDto = new SetmealDto();
        //先将菜品的普通属性拷贝到 setmealDto 中
        BeanUtils.copyProperties(setmeal, setmealDto);
        //创建条件构造器
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper();
        //利用套餐的id，查询菜品的信息
        queryWrapper.eq(SetmealDish::getSetmealId, id);
        List<SetmealDish> list = setmealDishService.list(queryWrapper);
        //将套餐的菜品信息补上
        setmealDto.setSetmealDishes(list);
        //返回
        return setmealDto;
    }

    //改菜品信息
    @Override
    @Transactional//因为该操作关系到多张表，所以要加入事务控制,保证数据的一致性
    public void updateWithDish(SetmealDto setmealDto) {
        //更新套餐表信息
        this.updateById(setmealDto);
        //获取保存的套餐的id
        Long setmealDtoId = setmealDto.getId();
        //清理当前的套餐菜品信息表中的数据
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(SetmealDish::getSetmealId, setmealDtoId);
        setmealDishService.remove(queryWrapper);
        //添加当前的菜品信息数据
        //获取套餐菜品的 List
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        //利用流遍历 flavors ，并设置每个元素中的 DishId，最后将流再转换成 List
        setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDtoId);
            return item;
        }).collect(Collectors.toList());
        //将剩余的口味信息批量保存到口味表
        setmealDishService.saveBatch(setmealDishes);
    }
}
