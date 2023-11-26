package com.mybot.bot;

import com.mybot.util.DataBaseConnector;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import telegram.CatBot;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {
    public static void main(String[] args) {
        try (Connection connection = DataBaseConnector.getConnection()) {
            System.out.println("Успешное соединение с базой данных");
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(new CatBot());
            // Выполнение запроса Select пример
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM memes_and_cats");

            while (resultSet.next()) {
                System.out.println("image_path: " + resultSet.getString("image_path"));
            }

        } catch (SQLException e) {
            System.out.println("Ошибка подключения к базе данных: " + e.getMessage());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
