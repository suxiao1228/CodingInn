package com.xiongsu.service.notify.service;

import com.xiongsu.api.event.MessageQueueEvent;

public interface RabbitmqService {

    boolean enabled();
    <T> void publishDirectMsg(MessageQueueEvent<T> messageQueueEvent, String key);

    <T> void publishFanoutMsg(MessageQueueEvent<T> messageQueueEvent);

    <T> void publishTopicMsg(MessageQueueEvent<T> messageQueueEvent, String key);


}
