package com.xiongsu.service.notify.service.impl;

import com.xiongsu.api.event.MessageQueueEvent;
import com.xiongsu.core.util.SpringUtil;
import com.xiongsu.service.notify.service.RabbitmqService;
import jakarta.annotation.Resource;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

public class RabbitmqServiceImpl implements RabbitmqService {

    @Resource
    private FanoutExchange fanoutExchange;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private DirectExchange directExchange;

    @Resource
    private TopicExchange topicExchange;

    //从配置文件中查询rabbitmq有没有开启
    @Override
    public boolean enabled() {
        return "true".equalsIgnoreCase(SpringUtil.getConfig("rabbitmq.switchFlag"));
    }

    //    @Override
//    public void publishMsg(String exchange,
//                           BuiltinExchangeType exchangeType,
//                           String toutingKey,
//                           String message) {
//        try {
//            //创建连接
//            RabbitmqConnection rabbitmqConnection = RabbitmqConnectionPool.getConnection();
//            Connection connection = rabbitmqConnection.getConnection();
//            //创建消息通道
//            Channel channel = connection.createChannel();
//            // 声明exchange中的消息为可持久化，不自动删除
//            channel.exchangeDeclare(exchange, exchangeType, true, false, null);
//            // 发布消息
//            channel.basicPublish(exchange, toutingKey, null, message.getBytes());
//            log.info("Publish msg: {}", message);
//            channel.close();
//            RabbitmqConnectionPool.returnConnection(rabbitmqConnection);
//        } catch (InterruptedException | IOException | TimeoutException e) {
//            e.printStackTrace();
//        }
//    }
    @Override
    public <T> void publishDirectMsg(MessageQueueEvent<T> messageQueueEvent, String key) {
        this.publishDirectMsg(messageQueueEvent, key, true);
    }

    private <T> void publishDirectMsg(MessageQueueEvent<T> messageQueueEvent , String key, boolean isPersist) {
        MessageProperties messageProperties = new MessageProperties();
        //根据 isPersist 选择 消息的投递模式：
        //PERSISTENT（持久化，消息存储到磁盘，即使 RabbitMQ 重启，消息不会丢失）。
        //NON_PERSISTENT（非持久化，消息只存储在内存，RabbitMQ 崩溃后消息丢失）。
        messageProperties.setDeliveryMode(isPersist? MessageDeliveryMode.PERSISTENT: MessageDeliveryMode.NON_PERSISTENT);

        Message message = rabbitTemplate.getMessageConverter().toMessage(messageQueueEvent, messageProperties);
        rabbitTemplate.convertAndSend(directExchange.getName(), key, message);//调用 rabbitTemplate.convertAndSend 发送消息（发送到 direct 交换机）。
    }

    //向 Fanout 交换机发送消息（广播模式）。
    //没有 routingKey，所有绑定到该交换机的队列都会收到消息。
    @Override
    public <T> void publishFanoutMsg(MessageQueueEvent<T> messageQueueEvent) {
        rabbitTemplate.convertAndSend(fanoutExchange.getName(), messageQueueEvent);
    }

    //向 Topic 交换机发送消息（主题匹配模式）。
    //需要提供 key（routing key），不同的队列会根据 绑定的 routing key 规则 选择性接收消息。
    @Override
    public <T> void publishTopicMsg(MessageQueueEvent<T> messageQueueEvent, String key) {
        rabbitTemplate.convertAndSend(topicExchange.getName(), key, messageQueueEvent);
    }
}
