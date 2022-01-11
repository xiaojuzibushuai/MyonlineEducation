package com.xuecheng.order.service;

import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.domain.task.XcTaskHis;
import com.xuecheng.order.dao.XcTaskHisRepository;
import com.xuecheng.order.dao.XcTaskRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @program: xcEduService01
 * @description:
 * @author: xiaojuzi
 * @create: 2020-12-21 12:06
 **/
@Service
public class TaskService {

    @Autowired
    XcTaskRepository xcTaskRepository;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    XcTaskHisRepository xcTaskHisRepository;


    /**
     * 查询前n条任务
     * @param updateTime
     * @param size
     * @return
     */
    public List<XcTask> findTaskList(Date updateTime, int size){
        //设置分页参数
        Pageable pageable = new PageRequest(0, size);

        Page<XcTask> all = xcTaskRepository.findByUpdateTimeBefore(pageable, updateTime);
        List<XcTask> content = all.getContent();
        return content;

    }

    /**
     * 发布消息
     * @param xcTask
     * @param exchange
     * @param routingkey
     */
    public void publish(XcTask xcTask,String exchange,String routingkey){
        Optional<XcTask> optionalXcTask = xcTaskRepository.findById(xcTask.getId());
        if (optionalXcTask.isPresent()){
            rabbitTemplate.convertAndSend(exchange,routingkey,xcTask);
            //更新任务时间
            XcTask xcTask1 = optionalXcTask.get();
            xcTask1.setUpdateTime(new Date());
            xcTaskRepository.save(xcTask1);
        }
    }

    /**
     * 获取任务
     * @param taskId
     * @param version
     * @return
     */
    @Transactional
    public int getTask(String taskId, int version){
        int i = xcTaskRepository.updateTaskVersion(taskId, version);
        return i;
    }

    /**
     * 完成任务
     * @param taskId
     */
    @Transactional
    public void finishTask(String taskId){
        Optional<XcTask> optionalXcTask = xcTaskRepository.findById(taskId);
        if (optionalXcTask.isPresent()){
            //当前任务
            XcTask xcTask = optionalXcTask.get();
            XcTaskHis xcTaskHis = new XcTaskHis();
            BeanUtils.copyProperties(xcTask,xcTaskHis);
            xcTaskHisRepository.save(xcTaskHis);
            xcTaskRepository.delete(xcTask);
        }

    }
}
