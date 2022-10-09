package com.cy.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cy.reggie.common.R;
import com.cy.reggie.entity.Category;
import com.cy.reggie.entity.Employee;
import com.cy.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController//处理请求
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    //新增菜品分类
    @PostMapping
    public R<String> save(@RequestBody Category category) {
        categoryService.save(category);
        return R.success("新增分类成功");
    }

    //菜品分类信息分页查询
    //Page是MybatisPlus封装的对象
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize) {
        //分页构造器
        Page<Category> pageInfo = new Page(page, pageSize);

        //条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper();

        //添加排序条件
        queryWrapper.orderByAsc(Category::getSort);

        //执行查询
        categoryService.page(pageInfo, queryWrapper);

        //数据会被封装到 pageInfo中
        return R.success(pageInfo);
    }

    //根据id删除菜品分类
    @DeleteMapping
    public R<String> delete(Long id) {
        //无判断直接删除分类
        //categoryService.removeById(id);

        //判断分类下是否有关联，再进行删除(用的是自己的方法)
        categoryService.remove(id);

        return R.success("删除成功");
    }

    //修改菜品分类信息
    @PutMapping
    public R<String> update(@RequestBody Category category) {
        categoryService.updateById(category);
        return R.success("分类信息修改成功");
    }

    //根据条件查询分类
    @GetMapping("/list")
    public R<List<Category>> list(Category category) {
        //条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper();
        //Type 等于传入的的 Type
        queryWrapper.eq(category.getType() != null, Category::getType, category.getType());
        //优先根据排序字段排序，排序字段相同，则根据更新时间排序
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        //传入条件，进行查询
        List<Category> list = categoryService.list(queryWrapper);
        //返回结果
        return R.success(list);
    }
}
