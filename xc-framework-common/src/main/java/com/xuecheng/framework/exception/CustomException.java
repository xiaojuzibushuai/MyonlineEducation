package com.xuecheng.framework.exception;

import com.xuecheng.framework.model.response.ResultCode;

/**
 * @program: xcEduService01
 * @description: 自定义异常
 * @author: xiaojuzi
 * @create: 2020-09-03 07:51
 **/
public class CustomException extends RuntimeException{
    ResultCode resultCode;

    public CustomException(ResultCode resultCode){
        this.resultCode=resultCode;
    }

    public ResultCode getResultCode(){
        return  resultCode;
    }
}
