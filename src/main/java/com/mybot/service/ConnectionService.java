package com.mybot.service;

import com.mybot.bot.CatBot;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public interface ConnectionService {
    Properties properties = new Properties();
    static void getConnect() {
        try (InputStream inputStream = CatBot.class.getClassLoader().getResourceAsStream("config.properties")) {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
