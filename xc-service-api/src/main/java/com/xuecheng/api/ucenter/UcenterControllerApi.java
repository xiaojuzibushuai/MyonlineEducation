package com.xuecheng.api.ucenter;

import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @program: xcEduService01
 * @description:
 * @author: xiaojuzi
 * @create: 2020-12-15 08:29
 **/
@Api(value = "用户中心",description = "用户中心管理接口")
public interface UcenterControllerApi {

    @ApiOperation("根据用户账号查询用户信息")
    public XcUserExt getUserext(String username);
}
