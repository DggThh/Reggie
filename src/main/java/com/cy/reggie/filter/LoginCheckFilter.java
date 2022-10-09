package com.cy.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.cy.reggie.common.BaseContext;
import com.cy.reggie.common.R;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

//检查用户是否登录
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")//指定拦截器名称，和拦截的url
public class LoginCheckFilter implements Filter {
    //路径匹配器,可匹配通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    //拦截器的实现类
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        //获取本次请求的 uri
        String requestURI = request.getRequestURI();

        //定义不需要处理的路径
        String[] urls = new String[]{
                "/employee/login",//登录
                "/employee/logout",//退出登录
                "/backend/**",//backend文件下的静态资源
                "/front/**",//front文件下的静态资源
                "/common/**",
                "/user/sendMsg",//移动端发送验证码
                "/user/login"//移动端登录
        };

        //判断是否要进行处理
        boolean check = check(urls, requestURI);

        //不需要就直接放行
        if (check) {
            filterChain.doFilter(request, response);
            return;
        }

        //（管理员）判断登录状态，如果已登录，则直接放行
        if (request.getSession().getAttribute("employee") != null) {
            //将当前登录用户的 id 存入 ThreadLocal 中
            BaseContext.setCurrentId((Long) request.getSession().getAttribute("employee"));
            filterChain.doFilter(request, response);
            return;
        }

        //（用户）判断登录状态，如果已登录，则直接放行
        if (request.getSession().getAttribute("user") != null) {
            //将当前登录用户的 id 存入 ThreadLocal 中
            BaseContext.setCurrentId((Long) request.getSession().getAttribute("user"));
            filterChain.doFilter(request, response);
            return;
        }

        //未登录返回未登录的提示,通过输出流方式向客户端页面响应数据
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;
    }

    //检查该请求路径是否需要进行拦截
    //true——放行；false——拦截
    public boolean check(String[] urls, String requestURI) {
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);
            if (match)
                return true;//匹配上了
        }
        return false;//没有匹配上
    }
}
