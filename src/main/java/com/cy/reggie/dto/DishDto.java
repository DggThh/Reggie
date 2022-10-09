package com.cy.reggie.dto;

import com.cy.reggie.entity.Dish;
import com.cy.reggie.entity.DishFlavor;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

//继承了Dish类里的属性，又扩展了两个属性，用于服务层和展示层数据的传递
@Data
public class DishDto extends Dish {
    //该属性封装了菜品的口味，因为一个菜品可能有多个口味，所以用 List
    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;

    private Integer copies;
}
