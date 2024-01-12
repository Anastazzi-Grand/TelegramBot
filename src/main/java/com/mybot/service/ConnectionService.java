package com.mybot.service;

import com.mybot.bot.CatBot;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Интерфейс для взятия данных из файла config.properties.
 * */
public interface ConnectionService {
    Properties properties = new Properties();

    /**
     * Метод, загружает данные из указанного файла.
     * */
    static void getConnect() {
        try (InputStream inputStream = CatBot.class.getClassLoader().getResourceAsStream("config.properties")) {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
