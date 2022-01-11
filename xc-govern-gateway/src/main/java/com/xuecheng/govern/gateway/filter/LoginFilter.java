package com.xuecheng.govern.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.govern.gateway.service.AuthService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @program: xcEduService01
 * @description: 身份校验过滤器
 * @author: xiaojuzi
 * @create: 2020-12-15 13:12
 **/
@Component
public class LoginFilter extends ZuulFilter {
    @Autowired
    AuthService authService;

    @Override
    /*过滤器类型
     pre：请求在被路由之前 执行
     routing：在路由请求时调用
     post：在routing和errror过滤器之后调用
     error：处理请求时发生错误调用*/
    public String filterType() {
        return "pre";
    }

    @Override
    //过滤器序号,越小先被执行
    public int filterOrder() {
        return 0;
    }

    @Override
    //判断过滤器是否要被执行
    public boolean shouldFilter() {
        //返回true则执行过滤器
        return true;
    }

    @Override
    //过滤器执行的业务逻辑
    public Object run() throws ZuulException {
        RequestContext currentContext = RequestContext.getCurrentContext();
        //得到request
        HttpServletRequest request = currentContext.getRequest();
        //得到response
        HttpServletResponse response = currentContext.getResponse();
        //取cookie中的身份令牌
        String tokenFromCookie = authService.getTokenFromCookie(request);
         if (StringUtils.isEmpty(tokenFromCookie)){
             access_denied();
             return  null;
         }
        //取头信息的身份令牌
        String jwtFromHeader = authService.getJwtFromHeader(request);
         if (StringUtils.isEmpty(jwtFromHeader)){
             access_denied();
             return  null;
         }
        //验证redis身份令牌
        long expire = authService.getExpire(tokenFromCookie);
         if (expire<0){
             access_denied();
             return  null;
         }
        return null;
    }


    //拒绝访问
    private void access_denied(){
        RequestContext requestContext = RequestContext.getCurrentContext();
        //得到response
        HttpServletResponse response = requestContext.getResponse();
        //拒绝访问
        requestContext.setSendZuulResponse(false);
        //设置响应代码
        requestContext.setResponseStatusCode(200);
        //构建响应信息
        ResponseResult responseResult = new ResponseResult(CommonCode.UNAUTHENTICATED);
        //转成json
        String jsonString = JSON.toJSONString(responseResult);
        requestContext.setResponseBody(jsonString);
        //设置contentType
        response.setContentType("application/json;charset=utf-8");
    }

}
