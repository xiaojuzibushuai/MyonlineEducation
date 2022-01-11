package com.xuecheng.test.rabbitmq;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @program: xcEduService01
 * @description:
 * @author: xiaojuzi
 * @create: 2020-10-20 09:30
 **/
public class Consumer2_subscribe_email {

    //队列
    private static final String QUEUE_INFORM_EMAIL = "queue_inform_email";
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
        try {
            connection = connectionFactory.newConnection();
            //设置会话通道 生产者和mq的通信channel
            Channel channel = connection.createChannel();
            //声明队列 参数1 队列名称 参数2 是否持久化
            // 参数3 exclusive是否独占连接 队列只允许在该连接中访问 队列关闭则删除
            //参数4 autoDelete 是否自动删除 设置为true可以实现临时队列
            //参数5 arguments 参数 可以设置一个扩展参数 如 生存时间
            channel.queueDeclare(QUEUE_INFORM_EMAIL, true, false, false, null);

            //声明交换机
            channel.exchangeDeclare(EXCHANGE_FANOUT_INFORM,BuiltinExchangeType.FANOUT);
            //进行交换机和队列绑定 参数1 队列名 参数2 交换机名 参数3 路由key 交换机根据路由key将消息换发到指定队列在发布订阅模式中为空串
            channel.queueBind(QUEUE_INFORM_EMAIL,EXCHANGE_FANOUT_INFORM,"");
            //定义消费者方法
            DefaultConsumer defaultConsumer = new DefaultConsumer(channel){
               //当接到消息后此方法调用
                /**
                 * @param consumerTag 消费者标签 标识消费者
                 * @param envelope 信封
                 * @param properties 消息属性
                 * @param body 消息内容
                 * @throws IOException
                 */

                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    //交换机
                    String exchange = envelope.getExchange();
                    //消息id
                    long deliveryTag = envelope.getDeliveryTag();
                    //消息内容
                    String message = new String(body,"utf-8");
                    System.out.println("recive message+"+message);

                }
            };
            //监听队列
            //参数1 队列名称  参数2 autoACK自动回复 参数3 callback 消费方法
            channel.basicConsume(QUEUE_INFORM_EMAIL,true,defaultConsumer);

        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
