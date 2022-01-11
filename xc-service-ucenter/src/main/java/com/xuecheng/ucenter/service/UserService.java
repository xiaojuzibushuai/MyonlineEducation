package com.xuecheng.ucenter.service;

import com.xuecheng.framework.domain.ucenter.XcCompanyUser;
import com.xuecheng.framework.domain.ucenter.XcMenu;
import com.xuecheng.framework.domain.ucenter.XcUser;
import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import com.xuecheng.ucenter.dao.XcCompanyUserRepository;
import com.xuecheng.ucenter.dao.XcMenuMapper;
import com.xuecheng.ucenter.dao.XcUserRepository;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @program: xcEduService01
 * @description:
 * @author: xiaojuzi
 * @create: 2020-12-15 08:37
 **/
@Service
public class UserService {
    @Autowired
    XcCompanyUserRepository xcCompanyUserRepository;
    @Autowired
    XcUserRepository xcUserRepository;
    @Autowired
    XcMenuMapper xcMenuMapper;

    //根据账号查询用户信息
    public XcUserExt getUserext(String username) {
        //根据账号信息查询xcUser信息
        XcUser xcUser = this.findXcUserByUsername(username);
        if (xcUser==null){
            return null;
        }
        //用户id
        String userId = xcUser.getId();
        //查询用户所有权限
        List<XcMenu> xcMenus = xcMenuMapper.selectPermissionByUserId(userId);
        //根据用户id查询所属公司id
        XcCompanyUser xcCompanyUser = xcCompanyUserRepository.findByUserId(xcUser.getId());
        //取到用户公司id
        String companyId=null;
        if (xcCompanyUser!=null){
            companyId= xcCompanyUser.getCompanyId();
        }
        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(xcUser,xcUserExt);
        xcUserExt.setCompanyId(companyId);
        //设置权限
        xcUserExt.setPermissions(xcMenus);
        return  xcUserExt;
    }

    //根据账号查询XcUser信息
    public XcUser findXcUserByUsername(String username) {
      return  xcUserRepository.findByUsername(username);
}
}
