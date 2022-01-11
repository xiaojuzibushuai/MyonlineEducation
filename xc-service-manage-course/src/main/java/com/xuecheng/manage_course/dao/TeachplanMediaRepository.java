package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.TeachplanMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @program: xcEduService01
 * @description:
 * @author: xiaojuzi
 * @create: 2020-12-08 16:45
 **/
public interface TeachplanMediaRepository extends JpaRepository<TeachplanMedia,String> {

    //根据课程id查询列表
   List<TeachplanMedia> findByCourseId(String courseId);

}
