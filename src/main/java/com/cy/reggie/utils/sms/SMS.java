package com.cy.reggie.utils.sms;

import org.apache.http.HttpResponse;

import java.util.HashMap;
import java.util.Map;

public class SMS {

    public static void SM_1(String phone, String code) {
        String content = "code:" + code;
        Send(phone, content, "TPL_0000");
    }

    //该模板暂时无法使用
    @Deprecated
    public static void SM_2(String phone, String code,String time){
        String content="code:"+code+",expire_at:"+time;
        Send(phone, code, "TPL_0001");
    }

    private static void Send(String phone, String content,String type) {
        String host = "https://dfsns.market.alicloudapi.com";
        String path = "/data/send_sms";
        String method = "POST";
        String appcode = "你自己的AppCode";
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        //根据API的要求，定义相对应的Content-Type
        headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        Map<String, String> querys = new HashMap<String, String>();
        Map<String, String> bodys = new HashMap<String, String>();
        bodys.put("content", content);
        bodys.put("phone_number", phone);
        bodys.put("template_id", type);


        try {
            /**
             * 重要提示如下:
             * HttpUtils请从
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java
             * 下载
             *
             * 相应的依赖请参照
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
             */
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            System.out.println(response.toString());
            //获取response的body
            //System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
