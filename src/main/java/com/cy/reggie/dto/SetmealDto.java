package com.cy.reggie.dto;

import com.cy.reggie.entity.Setmeal;
import com.cy.reggie.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
