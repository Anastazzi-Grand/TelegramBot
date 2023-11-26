package com.mybot.util;

import telegram.CatBot;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DataBaseConnector {
    static Properties props = new Properties();

    public static Connection getConnection() throws SQLException {

        try (InputStream inputStream = CatBot.class.getClassLoader().getResourceAsStream("config.properties")) {
            props.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String url = props.getProperty("db.url");
        String user = props.getProperty("db.user");
        String password = props.getProperty("db.password");

        return DriverManager.getConnection(url, user, password);
    }
}

