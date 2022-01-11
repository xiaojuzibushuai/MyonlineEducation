package com.xuecheng.test.rabbitmq;

import com.xuecheng.test.rabbitmq.config.RabbitmqConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @program: xcEduService01
 * @description:
 * @author: xiaojuzi
 * @create: 2020-10-20 17:04
 **/
@SpringBootTest
@RunWith(SpringRunner.class)
public class Producer5_topics_springboot {

    @Autowired
    RabbitTemplate rabbitTemplate;

    String message  = "send email message to user";
    //使用template发送消息
    @Test
    public void testSendEmail(){
        //参数1 交换机名称  2.routingkey 3.消息内容
        rabbitTemplate.convertAndSend(RabbitmqConfig.EXCHANGE_TOPICS_INFORM,"inform.email",message);
    }

}
