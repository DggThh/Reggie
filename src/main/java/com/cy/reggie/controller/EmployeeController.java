package com.cy.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cy.reggie.common.R;
import com.cy.reggie.entity.Employee;
import com.cy.reggie.service.EmployeeService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController//处理请求
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    //登录
    //request：用于获取session之后将员工id存入session
    // employee：获取传入数据
    @PostMapping("login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {
        //密码进行md5加密
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //判断用户是否存在
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);//UserName添加了唯一索引
        if (emp == null)
            return R.error("登录失败");

        //密码对比
        if (!password.equals(emp.getPassword()))
            return R.error("登录失败");

        //账号是否被禁用
        if (emp.getStatus() == 0)//0:禁用   1：可用
            return R.error("账号暂不可用");

        //账号、密码正确，且账号可用，登录成功
        request.getSession().setAttribute("employee", emp.getId());//将该员工的登录信息存入Session
        return R.success(emp);//只有登录成功了，密码才会被传回
    }

    //退出账号
    @PostMapping("logout")
    public R<String> logout(HttpServletRequest request) {
        //清除 Session 中的员工id
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    //新增员工
    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee) {
        //设置初始密码，并进行MD5加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        // region 由MP的元数据自动填充完成，无需手写
        //创建时间设置为当前时间
        //employee.setCreateTime(LocalDateTime.now());

        //更新时间
        //employee.setUpdateTime(LocalDateTime.now());

        //创建对象
        //Long empId = (Long) request.getSession().getAttribute("employee");
        //employee.setCreateUser(empId);

        //更新人
        //employee.setUpdateUser(empId);
        // endregion

        //存入对象
        employeeService.save(employee);

        log.info("员工信息：" + employee.toString());
        return R.success("成功新增员工");
    }

    //员工信息分页查询
    //Page是MybatisPlus封装的对象
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        //分页构造器
        Page pageInfo = new Page(page, pageSize);

        //条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);

        //添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        //执行查询
        employeeService.page(pageInfo, queryWrapper);

        //数据会被封装到 pageInfo中
        return R.success(pageInfo);
    }

    //根据id修改员工信息
    @PutMapping
    public R<String> update(HttpServletRequest request, @RequestBody Employee employee) {

        //region 由MP的元数据自动填充完成，无需手写
        //修改哪个用户进行的修改
        //employee.setUpdateUser((Long) request.getSession().getAttribute("employee"));

        //更新修改时间
        //employee.setUpdateTime(LocalDateTime.now());
        //endregion

        //根据id更新
        employeeService.updateById(employee);

        return R.success("员工信息更新成功");
    }

    //修改员工信息时数据的回写
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id) {
        Employee employee = employeeService.getById(id);
        if (employee != null)
            return R.success(employee);
        return R.error("没有查询到对应员工信息");
    }
}
