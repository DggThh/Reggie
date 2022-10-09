package com.cy.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cy.reggie.common.R;
import com.cy.reggie.entity.User;
import com.cy.reggie.service.UserService;
import com.cy.reggie.utils.ValidateCodeUtils;
import com.cy.reggie.utils.sms.SMS;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController//处理请求
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    //！！！！！！！！暂时关闭了验证码的发送！！！！！！！！
    //用户登录时，生成并发送验证码
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session) {
        //获取手机号
        String phone = user.getPhone();
        if (StringUtils.isNotEmpty(phone)) {
            //生成验证码
            String code = ValidateCodeUtils.generateValidateCode(6).toString();
            log.info("————————————————————————验证码为：" + code);
            //发送验证码
            SMS.SM_1(phone, code);

            //将验证码存入Redis中，有效期为 5 分钟
            redisTemplate.opsForValue().set(phone, code, 5, TimeUnit.MINUTES);
            return R.success("短信验证码发送成功");
        }
        return R.error("短信验证码发送失败");
    }

    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session) {
        //获取手机号
        String phone = map.get("phone").toString();
        //获取用户输入的验证码
        String code = map.get("code").toString();
        //从Session中获取验证码
        //String codeInSession = session.getAttribute(phone).toString();
        //从Redis中获取验证码
        String codeInSession =(String) redisTemplate.opsForValue().get(phone);

        //Session中验证码与用户输入的验证码进行比对
        if (codeInSession != null && codeInSession.equals(code)) {
            //登录成功后，要判断该手机号是否在用户表中，如果不在还要保存该手机号
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper();
            queryWrapper.eq(User::getPhone, phone);
            User user = userService.getOne(queryWrapper);
            if (user == null) {
                user = new User();
                user.setPhone(phone);
                userService.save(user);
            }
            session.setAttribute("user", user.getId());
            //登录成功删除缓存中的验证码
            redisTemplate.delete(phone);
            return R.success(user);
        }
        return R.error("登录失败");
    }

    @PostMapping("/loginout")
    public R<String> loginout(HttpSession session){
        session.removeAttribute("user");
        return R.success("账号以退出");
    }
}
