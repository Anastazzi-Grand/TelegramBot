package com.mybot.util;

import com.mybot.bot.CatBot;
import com.mybot.service.ConnectionService;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DataBaseConnector implements ConnectionService {
    public static Connection getConnection() throws SQLException {
        ConnectionService.getConnect();
        String url = ConnectionService.properties.getProperty("db.url");
        String user = ConnectionService.properties.getProperty("db.user");
        String password = ConnectionService.properties.getProperty("db.password");

        return DriverManager.getConnection(url, user, password);
    }
}

