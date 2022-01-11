package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.CoursePic;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @program: xcEduService01
 * @description:
 * @author: xiaojuzi
 * @create: 2020-10-25 10:46
 **/
public interface CoursePicRepository extends JpaRepository<CoursePic,String> {

    //当返回值>0 删除成功
    long  deleteByCourseid(String courseId);

}
