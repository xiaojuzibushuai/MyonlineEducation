package com.xuecheng.framework.exception;

import com.google.common.collect.ImmutableMap;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.model.response.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @program: xcEduService01
 * @description: 异常捕获类
 * @author: xiaojuzi
 * @create: 2020-09-03 07:59
 **/
//控制增强
@ControllerAdvice
public class ExceptionCatch {

    private static final Logger LOGGER =  LoggerFactory.getLogger(ExceptionCatch.class);

    //定义map 配置异常类型所对应的错误代码
    private static ImmutableMap<Class<? extends Throwable>,ResultCode> EXCEPTIONS;
    //定义builder对象 构建ImmutableMap
    protected static ImmutableMap.Builder<Class<? extends Throwable>,ResultCode> builder = ImmutableMap.builder();


    //捕获CustomException异常
    @ExceptionHandler(CustomException.class)
    @ResponseBody
    public ResponseResult customException(CustomException customException){
        customException.printStackTrace();
        //记录日志
        LOGGER.error("catch exception:{}",customException.getMessage());
        ResultCode  resultCode = customException.getResultCode();
        return new ResponseResult(resultCode);
    }

    //捕获Exception异常
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseResult exception(Exception exception){
        exception.printStackTrace();
    //记录日志
        LOGGER.error("catch exception:{}",exception.getMessage());
    if(EXCEPTIONS==null){
        EXCEPTIONS = builder.build();//EXCEPTIONS构建成功
    }
    // 从EXCEPTIONS中找到异常类型对应的错误代码
        ResultCode resultCode = EXCEPTIONS.get(exception.getClass());
    if(resultCode !=null){
        return new ResponseResult(resultCode);
    }else {
           return new ResponseResult(CommonCode.SERVER_ERROR);
    }
    }

    static {
        //定义异常类型所对应的错误代码
        builder.put(HttpMessageNotReadableException.class,CommonCode.INVALID_PARAM);
    }
}
