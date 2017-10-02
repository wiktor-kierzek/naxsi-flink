package com.example.data;

import com.example.Settings;
import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig;

/**
 * Created by wiktor.kierzek@curency-one.com on 02.10.2017.
 */
public class RabbitMQ {
    public static RMQConnectionConfig getConfig() {
        return new RMQConnectionConfig.Builder()
                .setUri(Settings.get(Settings.Key.RABBITMQ_URI))
                .build();
    }
}
