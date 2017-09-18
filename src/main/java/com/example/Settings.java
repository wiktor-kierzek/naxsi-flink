package com.example;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

//rozumiem, ze zalozeniem projektu byl brak frameworkow do dependency injection
//jesli tak, to ok, ale niestety podejscie ze statycznymi metodami wymusi brzydkie testowanie (np. uzycie powermocka)
//ogolnie metody statyczne uzywalbym jedynie w ostatecznosci - praca z legacy code'm, na pewno nie przy tworzeniu nowego projektu
//rozwiazan do DI jest masa, jesli nie chcecie uzywac springa ludzie polecali np. https://github.com/google/guice
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
            //aplikacja nie powinna sie wylaczyc w tym momencie?
            //wyjatek jest polykany, a przeciez bez propsow aplikacja nie wstanie
            System.out.println("Can not load settings.properties");
        }
    }

    public static String get(String key) {
        return props.getProperty(key);
    }
}
