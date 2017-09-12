package com.example;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Settings {

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

    public static String get(String key) {
        return props.getProperty(key);
    }
}
