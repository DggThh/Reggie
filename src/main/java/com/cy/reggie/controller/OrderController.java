package com.cy.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cy.reggie.common.BaseContext;
import com.cy.reggie.common.R;
import com.cy.reggie.dto.OrdersDto;
import com.cy.reggie.dto.SetmealDto;
import com.cy.reggie.entity.*;
import com.cy.reggie.service.OrderDetailService;
import com.cy.reggie.service.OrderService;
import com.cy.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.jni.OS;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.sql.Time;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController//处理请求
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private ShoppingCartService shoppingCartService;

    //下单
    @PostMapping("submit")
    public R<String> submit(@RequestBody Orders orders){
        orderService.submit(orders);
        return R.success("下单成功");
    }

    //订单分页查询
    @GetMapping("page")
    public R<Page> page(int page, int pageSize, Long number , String beginTime,String endTime){
        //分页构造器
        Page<Orders> pageInfo = new Page(page, pageSize);

        //条件构造器
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper();

        queryWrapper.like(number!=null,Orders::getNumber,number);
        queryWrapper.ge(beginTime!=null,Orders::getOrderTime,beginTime);
        queryWrapper.le(endTime!=null,Orders::getOrderTime,endTime);

        //添加排序条件
        queryWrapper.orderByAsc(Orders::getOrderTime);
        //执行查询
        orderService.page(pageInfo, queryWrapper);

        //数据会被封装到 pageInfo中
        return R.success(pageInfo);
    }

    //更新订单状态
    @PutMapping
    public R<String> updateStatus(@RequestBody Orders orders) {
        Long id=orders.getId();
        Integer status=orders.getStatus();
        LambdaQueryWrapper<Orders> queryWrapper=new LambdaQueryWrapper();
        queryWrapper.eq(Orders::getId, id);
        Orders order = orderService.getOne(queryWrapper);
        order.setStatus(status);
        orderService.updateById(order);
        return R.success("订单状态已修改");
    }


    @GetMapping("/userPage")
    public R<Page> userPage(int page,int pageSize) {
        //分页构造器
        Page<Orders> pageInfo = new Page(page, pageSize);
        Page<OrdersDto> dtoPage = new Page<>();
        //条件构造器
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper();
        //添加排序条件
        queryWrapper.orderByDesc(Orders::getOrderTime);
        //执行查询
        orderService.page(pageInfo, queryWrapper);
        //对象拷贝
        BeanUtils.copyProperties(pageInfo, dtoPage, "records");
        //获取 records
        List<Orders> records = pageInfo.getRecords();
        List<OrdersDto> list = records.stream().map((item) -> {
            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(item, ordersDto);
            //Long currentId = BaseContext.getCurrentId();
            Long orderId = ordersDto.getId();
            LambdaQueryWrapper<OrderDetail> orderDetailQueryWrapper = new LambdaQueryWrapper();
            orderDetailQueryWrapper.eq(OrderDetail::getOrderId, orderId);
            ordersDto.setOrderDetails(orderDetailService.list(orderDetailQueryWrapper));
            return ordersDto;
        }).collect(Collectors.toList());
        dtoPage.setRecords(list);
        //数据会被封装到 pageInfo中
        return R.success(dtoPage);
    }

    @PostMapping("/again")
    public R<String> again(@RequestBody Orders orders){
        Long id=orders.getId();
        LambdaQueryWrapper<OrderDetail> orderDetailLambdaQueryWrapper=new LambdaQueryWrapper();
        orderDetailLambdaQueryWrapper.eq(OrderDetail::getOrderId,id);
        List<OrderDetail> orderDetails = orderDetailService.list(orderDetailLambdaQueryWrapper);
        for (OrderDetail orderDetail:orderDetails) {
            ShoppingCart shoppingCart=new ShoppingCart();
            Long currentId = BaseContext.getCurrentId();
            shoppingCart.setUserId(currentId);
            shoppingCart.setName(orderDetail.getName());
            shoppingCart.setImage(orderDetail.getImage());
            shoppingCart.setDishId(orderDetail.getDishId());
            shoppingCart.setSetmealId(orderDetail.getSetmealId());
            shoppingCart.setDishFlavor(orderDetail.getDishFlavor());
            shoppingCart.setNumber(orderDetail.getNumber());
            shoppingCart.setAmount(orderDetail.getAmount());
            shoppingCartService.save(shoppingCart);
        }
        return R.success("相应菜品以添加至购物车中");
    }
}
