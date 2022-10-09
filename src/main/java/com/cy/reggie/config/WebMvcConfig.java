package com.cy.reggie.config;

import com.cy.reggie.common.JacksonObjectMapper;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport {
    //设置静态资源映射
    /*
    由于此处静态资源的两个文件加是直接存放到 resources 文件下
    而，tomcat 默认去 static 文件下查找静态资源，所以此处要重新设置静态资源的映射
     */
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        //将 /backend/** 的路径全部映射到 resources/backend/ 下
        registry.addResourceHandler("/backend/**").addResourceLocations("classpath:/backend/");
        //将 /front/** 的路径全部映射到 resources/front/ 下
        registry.addResourceHandler("/front/**").addResourceLocations("classpath:/front/");
    }

    //扩展 MVC 框架的消息转换器
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        //创建消息转换器对象
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        //设置对象转换器，底层使用 Jackson 将 Java 对象转换位 JSON
        messageConverter.setObjectMapper(new JacksonObjectMapper());
        //将这个消息转换器追加到MVC框架的转换器集合中
        //因为转换器有位置设定，如果直接追加会追加到最后，那样还是会优先使用前面的那些默认的转换器
        //0，表示追加到下标为 0 的位置上
        converters.add(0, messageConverter);
    }
}
