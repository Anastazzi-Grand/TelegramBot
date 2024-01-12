package com.mybot.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mybot.bot.CatBot;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


/**
 * Класс, реализующий логику для кнопки "Показать котят"
 * */
public class RandomCatPhotosService {
    static {
        ConnectionService.getConnect();
    }

    private static String CAT_API_KEY = ConnectionService.properties.getProperty("API_KEY");
    private static final String CAT_API_URL = "https://api.thecatapi.com/v1/images/search";

    /**
     * Метод соединяет бота с сервисом CatApi по ключу и получает ссылку на фото через JSON-объект.
     * */
    private String getRandomCatImageUrl() throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(CAT_API_URL)
                .addHeader("x-api-key", CAT_API_KEY)
                .get()
                .build();

        Response response = client.newCall(request).execute();
        String jsonData = response.body().string();
        System.out.println(jsonData);
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(jsonData).getAsJsonArray().get(0).getAsJsonObject();
        return jsonObject.get("url").getAsString();
    }

    /**
     * Метод отправляет полученное фото из сервиса.
     *
     * @param chatId
     * */
    public SendPhoto sendRandomCatPhoto(String chatId) throws IOException {
        String imageUrl = getRandomCatImageUrl();
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId);
        sendPhoto.setPhoto(new InputFile(imageUrl));

        return sendPhoto;
    }
}
