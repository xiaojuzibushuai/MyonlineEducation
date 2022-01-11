package com.xuecheng.auth;

import com.xuecheng.framework.client.XcServiceList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

/**
 * @program: xcEduService01
 * @description:
 * @author: xiaojuzi
 * @create: 2020-12-14 11:00
 **/
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestClient {


    @Autowired
    LoadBalancerClient loadBalancerClient;
    @Autowired
    RestTemplate restTemplate;

    //远程调用spring security获取令牌
    @Test
    public void testClient(){
        //从Eureka获取认证服务器地址
        ServiceInstance serviceInstance = loadBalancerClient.choose(XcServiceList.XC_SERVICE_UCENTER_AUTH);
        URI uri = serviceInstance.getUri();
        //令牌申请地址
        String authUrl=uri+"/auth/oauth/token";
        //定义header
        LinkedMultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        String httpBasic = getHttpBasic("XcWebApp", "XcWebApp");
        header.add("Authorization",httpBasic);
        //定义body
        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type","password");
        body.add("username","xiaojuzi");
        body.add("password","xiaojuzi");


        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(body,header);

        //设置restTemplate远程调用400 401 时候不报错
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler(){
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                if (response.getRawStatusCode()!=400 && response.getRawStatusCode()!=401)
                {
                    super.handleError(response);
                }
            }
        });
        ResponseEntity<Map> exchange = restTemplate.exchange(authUrl, HttpMethod.POST, httpEntity, Map.class);
        //申请令牌信息
        Map content = exchange.getBody();
        System.out.println(content);
    }


    //获取httpbasic的串
    private String getHttpBasic(String clientId,String clientSecret){
         String s1=clientId+":"+clientSecret;
        byte[] encode = Base64Utils.encode(s1.getBytes());
          return "Basic "+new String(encode);
    }

    @Test
    public void testPasswordEncoder(){
        //原始密码
        String password = "123456";
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        //使用BCrypt加密
        for (int i =0;i<10;i++){
            String encode = bCryptPasswordEncoder.encode(password);
            System.out.println(encode);
            //校验
            boolean matches = bCryptPasswordEncoder.matches(password, encode);
            System.out.println("是否匹配："+matches);
        }
    }
}
