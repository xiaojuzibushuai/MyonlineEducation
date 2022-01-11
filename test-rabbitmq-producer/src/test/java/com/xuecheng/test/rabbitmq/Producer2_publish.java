package com.xuecheng.test.rabbitmq;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @program: xcEduService01
 * @description:
 * @author: xiaojuzi
 * @create: 2020-10-20 09:08
 **/
public class Producer2_publish {

    //队列名称
    private static final String QUEUE_INFORM_EMAIL = "queue_inform_email";
    private static final String QUEUE_INFORM_SMS = "queue_inform_sms";
    private static final String EXCHANGE_FANOUT_INFORM="exchange_fanout_inform";

    public static void main(String[] args) {
        //通过工长创建新的连接和mq连接
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("127.0.0.1");
        connectionFactory.setPort(5672);//端口
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        //设置连接虚拟机 一个mq服务可以设置多个虚拟机 每个虚拟机相当于一个独立的mq
        connectionFactory.setVirtualHost("/");
        //建立新连接
        Connection connection = null;
        Channel channel = null;
        try {
            connection = connectionFactory.newConnection();
            //设置会话通道 生产者和mq的通信channel
             channel = connection.createChannel();
            //声明队列 参数1 队列名称 参数2 是否持久化
            // 参数3 exclusive是否独占连接 队列只允许在该连接中访问 队列关闭则删除
            //参数4 autoDelete 是否自动删除 设置为true可以实现临时队列
            //参数5 arguments 参数 可以设置一个扩展参数 如 生存时间
            channel.queueDeclare(QUEUE_INFORM_SMS,true,false,false,null);
            channel.queueDeclare(QUEUE_INFORM_EMAIL,true,false,false,null);
            //声明交换机
           /* 参数1 交换机名称 参数2 交换机类型
            fanout对应 rabbitmq 工作模式的 publish/subscribe
            direct 对应Routing 工作模式
            topic 对应 Topics工作模式
            headers 对应headers 工作模式*/
            channel.exchangeDeclare(EXCHANGE_FANOUT_INFORM, BuiltinExchangeType.FANOUT);
            //进行交换机和队列绑定 参数1 队列名 参数2 交换机名 参数3 路由key 交换机根据路由key将消息换发到指定队列在发布订阅模式中为空串
            channel.queueBind(QUEUE_INFORM_EMAIL,EXCHANGE_FANOUT_INFORM,"");
            channel.queueBind(QUEUE_INFORM_SMS,EXCHANGE_FANOUT_INFORM,"");
            //发送消息
              //参数明细 参数1 exchange 交换机 如果不指定将使用mq默认交换机
            // 参数2 routingKey 路由key 交换机根据路由来将消息转发到指定队列 默认交换机的路由key为队列名称
            //参数3 props 消息的属性 参数4 body消息内容
            for (int i = 0; i < 6; i++) {
                String message ="send inform message to user"+i;
                channel.basicPublish(EXCHANGE_FANOUT_INFORM,"",null,message.getBytes());
                System.out.println("send to mq +"+message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }finally {
            //关闭通道
            try {
                channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
            //关闭连接
            try {
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
