package com.xuecheng.framework.exception;

import com.xuecheng.framework.model.response.ResultCode;

/**
 * @program: xcEduService01
 * @description: 异常抛出类
 * @author: xiaojuzi
 * @create: 2020-09-03 07:55
 **/
public class ExceptionCast {

    public static void cast(ResultCode resultCode){
        throw  new CustomException(resultCode);
    }

}
