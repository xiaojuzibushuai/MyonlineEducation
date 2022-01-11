package com.xuecheng.framework.domain.course.ext;

import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.CourseMarket;
import com.xuecheng.framework.domain.course.CoursePic;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.Data;
import java.io.Serializable;

/**
 * @program: xcEduService01
 * @description:
 * @author: xiaojuzi
 * @create: 2020-10-25 17:13
 **/
@Data
@NoArgsConstructor
@ToString
public class CourseView implements Serializable {

    //课程基础信息
    private CourseBase courseBase;
    //课程图片
    private CoursePic coursePic;
    //课程营销
    private CourseMarket courseMarket;
    //教学计划
    private TeachplanNode teachplanNode;

}
