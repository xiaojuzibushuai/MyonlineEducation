package com.xuecheng.order.mq;

import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.order.config.RabbitMQConfig;
import com.xuecheng.order.service.TaskService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;


/**
 * @program: xcEduService01
 * @description:
 * @author: xiaojuzi
 * @create: 2020-12-21 12:10
 **/
@Component
public class ChooseCourseTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChooseCourseTask.class);

    @Autowired
    TaskService taskService;
    @Autowired
    RabbitTemplate rabbitTemplate;


    @RabbitListener(queues = RabbitMQConfig.XC_LEARNING_FINISHADDCHOOSECOURSE)
    public void receiveFinishChoosecourseTask(XcTask xcTask){
        if (xcTask!=null && StringUtils.isNotEmpty(xcTask.getId())){
            LOGGER.info("receiveChoosecourseTask...{}",xcTask.getId());
            taskService.finishTask(xcTask.getId());
        }

    }

    @Scheduled(cron = "0 0/1 * * * * ")
    //定时发送加的选课任务 一分钟一次
    public void sendChoosecourseTask(){
        //得到一分钟之前的时间
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.set(GregorianCalendar.MINUTE,-1);
        Date time = calendar.getTime();
        List<XcTask> taskList = taskService.findTaskList(time, 100);
        //调用service发送消息,将选课信息发送给mq
        for (XcTask xcTask:taskList){
            //取出任务
            if (taskService.getTask(xcTask.getId(),xcTask.getVersion())>0){
                String mqExchange = xcTask.getMqExchange();
                String mqRoutingkey = xcTask.getMqRoutingkey();
                taskService.publish(xcTask,mqExchange,mqRoutingkey);
            }
        }

    }






}
