package com.xiongsu.core.rabbitmq;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author Louzai
 * @date 2023/5/10
 */
public class RabbitmqConnection {

    private Connection connection;

    public RabbitmqConnection(String host, int port, String userName, String password, String virtualhost) {
        ConnectionFactory connectionFactory = new ConnectionFactory();//负责创建连接
        connectionFactory.setHost(host);//设置 RabbitMQ 主机地址
        connectionFactory.setPort(port);//设置端口号
        connectionFactory.setUsername(userName);//设置用户名
        connectionFactory.setPassword(password);//设置密码
        connectionFactory.setVirtualHost(virtualhost);//设置虚拟主机
        try {
            connection = connectionFactory.newConnection();
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取链接
     *
     * @return
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * 关闭链接
     *
     */
    public void close() {
        try {
            connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
