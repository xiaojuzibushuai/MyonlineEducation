package com.xuecheng.ucenter.dao;

import com.xuecheng.framework.domain.ucenter.XcCompanyUser;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @program: xcEduService01
 * @description:
 * @author: xiaojuzi
 * @create: 2020-12-15 08:32
 **/
public interface XcCompanyUserRepository extends JpaRepository<XcCompanyUser,String> {

    //根据userId查询用户公司信息
    XcCompanyUser findByUserId(String userId);

}
