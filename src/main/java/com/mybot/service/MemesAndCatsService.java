package com.mybot.service;

import com.mybot.util.DataBaseConnector;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class MemesAndCatsService {

    DataBaseConnector databaseConnector;
    private String getRandomPhotoFromBD() {
        String photoUrl = null;
        try {
            Connection connection = databaseConnector.getConnection(); // Получаем соединение с базой данных
            PreparedStatement statement = connection.prepareStatement("SELECT image_path FROM memes_and_cats ORDER BY RANDOM() LIMIT 1");
            ResultSet resultSet = statement.executeQuery(); // Запрос на выбор рандомной фотографии из базы данных

            if (resultSet.next()) {
                photoUrl = resultSet.getString("image_path");
            }

            resultSet.close();
            statement.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return photoUrl;
    }

    public SendPhoto sendPhotoFromBD(String chatId) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId);
        sendPhoto.setPhoto(new InputFile(getRandomPhotoFromBD()));
        System.out.println("Send photo^ " + getRandomPhotoFromBD());
        return sendPhoto;
    }
}
