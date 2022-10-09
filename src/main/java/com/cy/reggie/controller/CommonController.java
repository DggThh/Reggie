package com.cy.reggie.controller;

import com.cy.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Filter;

//文件的上传于下载
@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {
    @Value("${reggie.path}")
    private String imgPath;

    //文件上传
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) {
        //file.getOriginalFilename() 表示获取原始文件名，但为了防止重名，不能用原始文件名
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf('.'));
        //使用uuid重命名文件名，防止文件覆盖
        String fileName = UUID.randomUUID().toString() + suffix;
        //为了防止存放文件的目录不存在，需要进行判断,不存在时需要创建相应目录
        File dir = new File(imgPath);
        if (!dir.exists())
            dir.mkdirs();
        try {
            //file 接收到文件后会存为一个临时文件，方法结束，临时文件会被删除，需要转存
            file.transferTo(new File(imgPath + fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return R.success(fileName);
    }

    //文件下载
    //void，不需要返回值是因为直接使用输出流的方式将文件写回浏览器即可
    //HttpServletResponse,利用 HttpServletResponse 将文件写回浏览器
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) {
        //通过输入流读取文件内容
        try {
            FileInputStream fileInputStream = new FileInputStream(new File(imgPath + name));
            //通过输出流，将文件写回浏览器
            ServletOutputStream outputStream = response.getOutputStream();
            //设置相应回去的文件类型
            response.setContentType("image/jpeg");
            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = fileInputStream.read(bytes)) != -1) {
                outputStream.write(bytes);
                outputStream.flush();
            }
            fileInputStream.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
