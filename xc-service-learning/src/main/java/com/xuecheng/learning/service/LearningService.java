package com.xuecheng.learning.service;

import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.learning.XcLearningCourse;
import com.xuecheng.framework.domain.learning.response.GetMediaResult;
import com.xuecheng.framework.domain.learning.response.LearningCode;
import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.domain.task.XcTaskHis;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.learning.client.CourseSearchClient;
import com.xuecheng.learning.dao.XcLearningCourseRepository;
import com.xuecheng.learning.dao.XcTaskHisRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.Optional;

/**
 * @program: xcEduService01
 * @description:
 * @author: xiaojuzi
 * @create: 2020-12-10 13:45
 **/
@Service
public class LearningService {

    @Autowired
    CourseSearchClient courseSearchClient;
    @Autowired
    XcLearningCourseRepository xcLearningCourseRepository;
    @Autowired
    XcTaskHisRepository xcTaskHisRepository;

    //获取课程
    public GetMediaResult getMedia(String courseId, String teachplanId) {
        //校验学生学习的权限

        //调用搜索服务
        TeachplanMediaPub teachplanMediaPub = courseSearchClient.getmedia(teachplanId);
        if (teachplanMediaPub==null || StringUtils.isEmpty(teachplanMediaPub.getMediaUrl())){
            //获取视频错误
            ExceptionCast.cast(LearningCode.LEARNING_GETMEDIA_ERROR);
        }
        return new GetMediaResult(CommonCode.SUCCESS,teachplanMediaPub.getMediaUrl());
    }

    /**
     * 添加选课
     * @param userId
     * @param courseId
     * @param valid
     * @param startTime
     * @param endTime
     * @param xcTask
     * @return
     */
    @Transactional
    public ResponseResult addCourse(String userId, String courseId,String valid, Date startTime, Date endTime, XcTask xcTask){
            if (StringUtils.isEmpty(courseId)){
                ExceptionCast.cast(LearningCode.LEARNING_GETMEDIA_ERROR);
            }
            if (StringUtils.isEmpty(userId)) {
                ExceptionCast.cast(LearningCode.CHOOSECOURSE_USERISNULL);
            }
            if(xcTask == null || StringUtils.isEmpty(xcTask.getId())){
                ExceptionCast.cast(LearningCode.CHOOSECOURSE_TASKISNULL);
            }
            //查询历史任务
        Optional<XcTaskHis> optionalXcTaskHis = xcTaskHisRepository.findById(xcTask.getId());
            if (optionalXcTaskHis.isPresent()){
                return new ResponseResult(CommonCode.SUCCESS);
            }
        XcLearningCourse xcLearningCourse = xcLearningCourseRepository.findByUserIdAndCourseId(userId, courseId);
            if (xcLearningCourse==null){
                xcLearningCourse = new XcLearningCourse();
                xcLearningCourse.setUserId(userId);
                xcLearningCourse.setCourseId(courseId);
                xcLearningCourse.setValid(valid);
                xcLearningCourse.setStartTime(startTime);
                xcLearningCourse.setEndTime(endTime);
                xcLearningCourse.setStatus("501001");
                xcLearningCourseRepository.save(xcLearningCourse);
            }else {
                xcLearningCourse.setValid(valid);
                xcLearningCourse.setStartTime(startTime);
                xcLearningCourse.setEndTime(endTime);
                xcLearningCourse.setStatus("501001");
                xcLearningCourseRepository.save(xcLearningCourse);
            }
            //向历史任务表插入记录
        Optional<XcTaskHis> optional = xcTaskHisRepository.findById(xcTask.getId());
            if (!optional.isPresent()){
                //添加历史任务
                XcTaskHis xcTaskHis = new XcTaskHis();
                BeanUtils.copyProperties(xcTask,xcTaskHis);
                xcTaskHisRepository.save(xcTaskHis);
            }
            return new ResponseResult(CommonCode.SUCCESS);
    }

}
