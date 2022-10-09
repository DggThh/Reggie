package com.cy.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cy.reggie.common.R;
import com.cy.reggie.dto.DishDto;
import com.cy.reggie.entity.Category;
import com.cy.reggie.entity.Dish;
import com.cy.reggie.entity.DishFlavor;
import com.cy.reggie.entity.Employee;
import com.cy.reggie.mapper.DishMapper;
import com.cy.reggie.service.CategoryService;
import com.cy.reggie.service.DishFlavorService;
import com.cy.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.statement.select.KSQLWindow;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

//关于菜品和菜品口味的操作
@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisTemplate redisTemplate;

    //新增菜品
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }

    //菜品信息分页
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        //分页构造器
        Page<Dish> pageInfo = new Page(page, pageSize);
        Page<DishDto> dishDtoPage = new Page(page, pageSize);
        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper();
        //添加过滤条件
        queryWrapper.like(name != null, Dish::getName, name);
        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        //执行查询
        dishService.page(pageInfo, queryWrapper);
        //对象拷贝(将除了 records 属性外的其他属性都拷贝到 dishDtoPage 中)
        //records中存的就是包含了一系列数据的List
        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");
        //获取 records
        List<Dish> records = pageInfo.getRecords();
        //将 pageInfo 中的 records 拷贝给 list，并为 list 中的每个元素查找并设置它们的 categoryId
        List<DishDto> list = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            Long categoryId = item.getCategoryId();//获取每个菜品的分类id
            Category category = categoryService.getById(categoryId);
            //防止查询不到菜品的分类
            if (category != null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());
        //传入 dishDtoPage 的 records
        dishDtoPage.setRecords(list);
        //数据会被封装到 pageInfo中
        return R.success(dishDtoPage);
    }

    //根据id进行菜品信息的回写
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id) {
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        if (dishDto != null)
            return R.success(dishDto);
        return R.error("没有查询到对应菜品信息");
    }


    //菜品信息的修改
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        //清理修改菜品属于的分类的缓存数据
        String key = "dish_" + dishDto.getCategoryId() + "_" + dishDto.getStatus();
        redisTemplate.delete(key);

        dishService.updateWithFlavor(dishDto);
        return R.success("菜品修改成功");
    }

/*    @GetMapping("/list")
    public R<List<Dish>> list(Dish dish) {
        Long categoryId = dish.getCategoryId();
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(categoryId != null, Dish::getCategoryId, categoryId);
        //只查询状态为1（起售）的菜品
        queryWrapper.eq(Dish::getStatus,1);
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> dishes = dishService.list(queryWrapper);
        return R.success(dishes);
    }*/
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish) {
        //创建要返回的对象
        List<DishDto> dishDtoList = null;
        //构造Key
        String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();

        //尝试从Redis中获取数据
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);
        if (dishDtoList != null) {
            //Redis中存在，就直接返回数据
            return R.success(dishDtoList);
        }

        //Redis中不存在，则查询数据库
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        //只查询状态为1（起售）的菜品
        queryWrapper.eq(Dish::getStatus, 1);
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> dishes = dishService.list(queryWrapper);

        dishDtoList = dishes.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            Long categoryId = item.getCategoryId();//获取每个菜品的分类id
            Category category = categoryService.getById(categoryId);
            //防止查询不到菜品的分类
            if (category != null) {
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            //获取当前菜品Id
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper();
            lambdaQueryWrapper.eq(DishFlavor::getDishId, dishId);
            List<DishFlavor> dishFlavorList = dishFlavorService.list(lambdaQueryWrapper);
            dishDto.setFlavors(dishFlavorList);
            return dishDto;
        }).collect(Collectors.toList());
        //从数据库中获取到了数据要存入缓存中(保存60分钟)
        redisTemplate.opsForValue().set(key, dishDtoList,60, TimeUnit.MINUTES);
        return R.success(dishDtoList);
    }

    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable("status") Integer status,Long[] ids){
        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper();
        queryWrapper.in(ids != null, Dish::getId, ids);
        List<Dish> dishes = dishService.list(queryWrapper);
        dishes.stream().map((item)->{
          item.setStatus(status);
          return item;
        }).collect(Collectors.toList());
        dishService.updateBatchById(dishes);
        return R.success("状态跟新完毕");
    }

    @DeleteMapping
    public R<String> logicDelete(Long[] ids) {
        dishService.deleteWithFlavor(ids);
        return R.success("删除成功");
    }
}
