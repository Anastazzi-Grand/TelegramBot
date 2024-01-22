package com.mybot.service;

import com.mybot.service.DB.DataBaseConnector;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Класс, реализущий логику кнопки "Мемы и коты"
 * */
public class MemesAndCatsService {

    /**
     * Метод соединяет с БД и возвращает рандомную ссылку на картинку типа String.
     * */
    private String getRandomPhotoFromBD() {
        String photoUrl = null;
        try {
            Connection connection = DataBaseConnector.getCatsDBConnection(); // Получаем соединение с базой данных
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

    /**
     * Метод отправляет фото из БД
     *
     * @param chatId
     * */
    public SendPhoto sendPhotoFromBD(String chatId) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId);
        sendPhoto.setPhoto(new InputFile(getRandomPhotoFromBD()));
        System.out.println("Send photo^ " + getRandomPhotoFromBD());
        return sendPhoto;
    }
}
