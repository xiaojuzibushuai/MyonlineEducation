package com.xuecheng.manage_cms.service;

import com.xuecheng.framework.domain.system.SysDictionary;
import com.xuecheng.manage_cms.dao.SysDictionaryDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @program: xcEduService01
 * @description:
 * @author: xiaojuzi
 * @create: 2020-10-23 12:05
 **/
@Service
public class SysDictionaryService {
    @Autowired
    SysDictionaryDao sysDictionaryDao;

    public SysDictionary findDictionaryByType(String type){
        return sysDictionaryDao.findBydType(type);
    }

}
