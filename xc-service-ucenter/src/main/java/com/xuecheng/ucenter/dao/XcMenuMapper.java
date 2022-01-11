package com.xuecheng.ucenter.dao;

import com.xuecheng.framework.domain.ucenter.XcMenu;
import org.apache.ibatis.annotations.Param;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * @program: xcEduService01
 * @description:
 * @author: xiaojuzi
 * @create: 2020-12-15 15:58
 **/
@Mapper
public interface XcMenuMapper {
    //根据用户id查询用户权限
    public List<XcMenu>  selectPermissionByUserId(@Param("userId") String userId);
}
