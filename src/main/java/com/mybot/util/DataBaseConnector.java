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
/*
 INSERT INTO memes_and_cats (image_path) VALUES
 ('https://disk.yandex.ru/i/cZbvRlzPcw6NgA'),
 ('https://disk.yandex.ru/i/KZmWCfGMcC2SYg'),
 ('https://disk.yandex.ru/i/zljWNFco7XD9Kg'),
 ('https://disk.yandex.ru/i/uQ3HOYCzc4VC1A'),
 ('https://disk.yandex.ru/i/GlmiNGp-F1clcg'),
 ('https://disk.yandex.ru/i/OuhS_kVzODMs9Q'),
 ('https://disk.yandex.ru/i/ci6uLySUBRw-tA'),
 ('https://disk.yandex.ru/i/iShCe5O5xrj5rQ'),
 ('https://disk.yandex.ru/i/O4jm1F5XItrJVw'),
 ('https://disk.yandex.ru/i/LN-_W3Ai_5L00Q');

 */

