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

public class RandomCatPhotosService {
    static Properties properties = new Properties();
    static {
        try (InputStream inputStream = CatBot.class.getClassLoader().getResourceAsStream("config.properties")) {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String CAT_API_KEY = properties.getProperty("API_KEY");
    private static final String CAT_API_URL = "https://api.thecatapi.com/v1/images/search";

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
    public SendPhoto sendRandomCatPhoto(String chatId) throws IOException {
        String imageUrl = getRandomCatImageUrl();
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId);
        sendPhoto.setPhoto(new InputFile(imageUrl));

        return sendPhoto;
    }
}
