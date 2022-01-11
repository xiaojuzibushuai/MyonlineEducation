package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.Teachplan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @program: xcEduService01
 * @description:
 * @author: xiaojuzi
 * @create: 2020-10-21 21:50
 **/
public interface TeachplanRepository extends JpaRepository<Teachplan,String> {
    //课程id和parentId查询teachplan
    public List<Teachplan> findByCourseidAndParentid(String courseId,String parentId);
}
