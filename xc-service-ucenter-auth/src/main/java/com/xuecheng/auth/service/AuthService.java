package com.xuecheng.auth.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.client.XcServiceList;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import com.xuecheng.framework.domain.ucenter.response.AuthCode;
import com.xuecheng.framework.exception.ExceptionCast;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @program: xcEduService01
 * @description:
 * @author: xiaojuzi
 * @create: 2020-12-14 11:54
 **/
@Service
public class AuthService {

    @Value("${auth.tokenValiditySeconds}")
    int tokenValiditySeconds;
    @Autowired
    LoadBalancerClient loadBalancerClient;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    /**
     * 用户认证申请令牌
     * @param username
     * @param password
     * @param clientId
     * @param clientSecret
     * @return
     */
    public AuthToken login(String username, String password, String clientId, String clientSecret) {

        //请求springsecurity申请令牌
        AuthToken authToken = this.applyToken(username, password, clientId, clientSecret);
        if (authToken==null){
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_ERROR);
        }
        //用户令牌
        String access_token = authToken.getAccess_token();
        //存储到redis中的内容
        String jsonString = JSON.toJSONString(authToken);
        //存储到令牌到redis
        boolean result = this.saveToken(access_token, jsonString, tokenValiditySeconds);
         if (!result){
             ExceptionCast.cast(AuthCode.AUTH_LOGIN_TOKEN_SAVEFAIL);
         }
         return  authToken;
    }

    //申请令牌
    private AuthToken applyToken(String username, String password, String clientId, String clientSecret){
        //从Eureka获取认证服务器地址
        ServiceInstance serviceInstance = loadBalancerClient.choose(XcServiceList.XC_SERVICE_UCENTER_AUTH);
        URI uri = serviceInstance.getUri();
        //令牌申请地址
        String authUrl=uri+"/auth/oauth/token";
        //定义header
        LinkedMultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        String httpBasic = getHttpBasic(clientId, clientSecret);
        header.add("Authorization",httpBasic);
        //定义body
        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type","password");
        body.add("username",username);
        body.add("password",password);

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
        if (content ==null || content.get("access_token")==null
                || content.get("refresh_token")==null
                || content.get("jti")==null){

            //解析springSecurity返回的信息
            String error_description = (String) content.get("error_description");
            if (error_description.indexOf("UserDetailService returned null")>=0){
                ExceptionCast.cast(AuthCode.AUTH_ACCOUNT_NOTEXISTS);
            }else if (error_description.indexOf("坏的凭证")>=0){
                ExceptionCast.cast(AuthCode.AUTH_CREDENTIAL_ERROR);
            }
            return null;
        }
        AuthToken authToken = new AuthToken();
        authToken.setAccess_token((String) content.get("jti"));//jwt令牌
        authToken.setJwt_token((String)content.get("access_token"));//用户身份令牌
        authToken.setRefresh_token((String)content.get("refresh_token"));//刷新令牌
        return  authToken;
    }

    /**
     * 存储到令牌到redis
     * @param access_token
     * @param content Auth对象的内容
     * @param ttl 过期时间
     * @return
     */
    private boolean saveToken(String access_token,String content,long ttl){
         String key = "user_token:"+access_token;
         stringRedisTemplate.boundValueOps(key).set(content,ttl, TimeUnit.SECONDS);
         Long expire = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
         return expire>0;
    }

    /**
     * 删除redis中的token
     * @param access_token
     * @return
     */
    public boolean deleteToken(String access_token){
        String key = "user_token:"+access_token;
        stringRedisTemplate.delete(key);
        return true;
    }

    /**
     * 从redis查询令牌
     * @param access_token
     * @return
     */
    public AuthToken getUserToken(String access_token){
        String key ="user_token:"+access_token;
        String value = stringRedisTemplate.opsForValue().get(key);
        //转成对象
        try {
            AuthToken authToken = JSON.parseObject(value, AuthToken.class);
            return authToken;
        }catch (Exception e){
            e.printStackTrace();
            return  null;
        }
    }



    //获取httpBasic的串
    private String getHttpBasic(String clientId,String clientSecret){
        String s1=clientId+":"+clientSecret;
        byte[] encode = Base64Utils.encode(s1.getBytes());
        return "Basic "+new String(encode);
    }
}
