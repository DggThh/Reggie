package com.cy.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cy.reggie.entity.DishFlavor;
import com.cy.reggie.mapper.DishFlavorMapper;
import com.cy.reggie.service.DishFlavorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements DishFlavorService {
    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Override
    public int permanentlyDelete(List<DishFlavor> dishFlavorList) {
        int count=0;
        for (DishFlavor dishFlavor :dishFlavorList) {
            count += dishFlavorMapper.permanentlyDelete(dishFlavor.getId());
        }
        return 0;
    }
}
