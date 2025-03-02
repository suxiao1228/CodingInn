package com.xiongsu.api.event;

import com.xiongsu.api.enums.NotifyTypeEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @program: pai_coding
 * @description: mq生产和消费的事件
 * @author: XuYifei
 * @create: 2024-10-31
 */

@Getter
@Setter
@ToString
@NoArgsConstructor
public class MessageQueueEvent<T> {
    private NotifyTypeEnum notifyType;

    private T content;

    private Long userId;


    public MessageQueueEvent(NotifyTypeEnum notifyType, T content) {
        this.notifyType = notifyType;
        this.content = content;
    }

    public MessageQueueEvent(NotifyTypeEnum notifyType, T content, Long userId) {
        this.notifyType = notifyType;
        this.content = content;
        this.userId = userId;
    }
}
