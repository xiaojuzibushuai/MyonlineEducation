package com.xuecheng.api.course;

import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.AddCourseResult;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @program: xcEduService01
 * @description:
 * @author: xiaojuzi
 * @create: 2020-10-21 20:12
 **/
@Api(value="课程管理接口",description = "课程管理接口，提供课程的增删改查")
public interface CourseControllerApi {
    @ApiOperation("课程计划查询")
    public TeachplanNode findTeachplanList(String courseId);

    @ApiOperation("添加课程计划")
    public ResponseResult addTeachplan(Teachplan teachplan);

    @ApiOperation("查询课程列表")
    public QueryResponseResult findCourseList(int page, int size, CourseListRequest courseListRequest);

    @ApiOperation("添加课程基础信息")
    public AddCourseResult addCourseBase(CourseBase courseBase);

    @ApiOperation("获取课程基础信息")
    public CourseBase getCourseBaseById(String courseId) throws RuntimeException;

    @ApiOperation("更新课程基础信息")
    public ResponseResult updateCourseBase(String id,CourseBase courseBase);

    @ApiOperation("获取课程营销信息")
    public CourseMarket getCourseMarketById(String courseId);

    @ApiOperation("更新课程营销信息")
    public ResponseResult updateCourseMarket(String id,CourseMarket courseMarket);

    @ApiOperation("添加课程图片")
    public ResponseResult addCoursePic(String courseId,String pic);

    @ApiOperation("查询课程图片")
    public CoursePic findCoursePic(String courseId);

    @ApiOperation("删除课程图片")
    public ResponseResult deleteCoursePic(String courseId);

    @ApiOperation("课程视图的查询")
    public CourseView findCourseView(String courseId);

    @ApiOperation("预览课程")
    public CoursePublishResult preview(String id);

    @ApiOperation("发布课程")
    public CoursePublishResult publish(String id);

    @ApiOperation("保存课程计划与媒资文件关联")
    public ResponseResult savemedia(TeachplanMedia teachplanMedia);
}

