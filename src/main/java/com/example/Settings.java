package com.example;

import lombok.AllArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Settings {

    @AllArgsConstructor
    public enum Key {
        RABBITMQ_URI("amqp.uri"),
        RABBITMQ_NAXSI_QUEUE("amqp.name"),

        ELASTIC_CLUSTER_NAME("elastic.cluster.name"),
        ELASTIC_MAX_BULK_SIZE("elastic.max.bulk.size"),
        ELASTIC_PORT("elastic.port"),
        ELASTIC_HOSTS("elastic.host"),
        ELASTIC_NAXSI_INDEX_NAME("elastic.index.name"),
        ELASTIC_NAXSI_INDEX_TYPE("elastic.index.type"),

        OPSGENIE_API_KEY("opsgenie.api.key"),
        OPSGENIE_API_TEAM("opsgenie.api.team");


        String name;

        @Override
        public String toString() {
            return this.name;
        }
    }

    private static Properties props;

    static {
        InputStream inputStream = Settings.class.getClassLoader().getResourceAsStream("settings.properties");
        load(inputStream);
    }

    private static void load(InputStream inputStream) {
        props = new Properties();
        try {
            props.load(inputStream);

        } catch (IOException e) {
            System.out.println("Can not load settings.properties");
        }
    }

    public static String get(Key key) {
        return props.getProperty(key.toString());
    }
}
