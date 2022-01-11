package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.system.SysDictionary;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @program: xcEduService01
 * @description:
 * @author: xiaojuzi
 * @create: 2020-10-23 12:04
 **/
public interface SysDictionaryDao extends MongoRepository<SysDictionary,String> {

    //根据字典分类查询字典信息
    SysDictionary findBydType(String dType);
}
