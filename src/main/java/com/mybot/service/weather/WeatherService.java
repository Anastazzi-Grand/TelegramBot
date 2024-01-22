package com.mybot.service.weather;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mybot.service.ConnectionService;
import com.mybot.service.state_manager.CommandState;
import com.mybot.service.state_manager.CommandStateManager;
import org.telegram.telegrambots.meta.api.objects.Message;

import javax.crypto.spec.PSource;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class WeatherService {
    static {
        ConnectionService.getConnect();
    }

    private CommandStateManager commandStateManager;
    private static final String YANDEX_WEATHER = ConnectionService.properties.getProperty("api_weather");

    private static final String YANDEX_GEOCODER = ConnectionService.properties.getProperty("api_geocoder");

    public WeatherService(CommandStateManager commandStateManager) {
        this.commandStateManager = commandStateManager;
    }

    /**
     * Метод для считывания сообщения пользователя.
     * Возвращает строку.
     *
     * @param message
     * @param chatId
     * */
    private String processUserMessage(String chatId, Message message) {
        String location = "";
        if (commandStateManager.isWaiting(chatId, CommandState.WEATHER)) {
            commandStateManager.setWaiting(chatId, CommandState.WEATHER, false);
            location = message.getText(); // Обработка ввода названия региона/города
        }

        return location;
    }

    /**
     * Метод, который получает широту и долготу отправленного в сообщении региона/города через Яндекс API Геокодер.
     * */
    private String getYandexGeocoder(String chatId, Message message) {
        try {
            String location = processUserMessage(chatId, message).toLowerCase();
            if (!location.isEmpty()) {
                String encodedLocation = URLEncoder.encode(location, "UTF-8");
                URL url = new URL("https://geocode-maps.yandex.ru/1.x/?apikey=" + YANDEX_GEOCODER + "&geocode=" + encodedLocation + "&format=json");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                String coordinates = "";
                JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
                if (checkYandexGeocoder(jsonResponse)) {
                    coordinates = jsonResponse.getAsJsonObject("response")
                            .getAsJsonObject("GeoObjectCollection")
                            .getAsJsonArray("featureMember").get(0)
                            .getAsJsonObject().getAsJsonObject("GeoObject")
                            .getAsJsonObject("Point").get("pos").getAsString();
                    System.out.println("МЕТОД getYandexGeocoder НАЗВАНИЕ" + location);
                    System.out.println("МЕТОД getYandexGeocoder КООРДИНАТЫ" + coordinates);
                    commandStateManager.setWaiting(chatId, CommandState.WEATHER, false);
                    System.out.println("COORDINATES " + coordinates);
                    return coordinates + " " + location;
                } else {
                    return "INVALID";
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "INVALID";
        }
        return "INVALID";
    }

    /**
     * Метод отправляет координаты региона в Яндекс Погоду для получения сведений о погоде.
     * */
    private String getYandexWeather(String chatId, Message message) {
        String coordinates = getYandexGeocoder(chatId, message);

        if (!coordinates.equals("INVALID")) {
            String apiUrl = "https://api.weather.yandex.ru/v2/forecast?lat=" + coordinates.split(" ")[1] + "&lon=" + coordinates.split(" ")[0] + "&lang=ru_RU";
            String loc = coordinates.substring(coordinates.split(" ")[1].length() + coordinates.split(" ")[0].length() + 2);

            try {
                // Выполняем HTTP-запрос к API Яндекс Погоды
                URL url = new URL(apiUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("X-Yandex-API-Key", YANDEX_WEATHER);

                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Парсим JSON-ответ
                JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
                JsonObject fact = jsonResponse.getAsJsonObject("fact");

                String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                String temperature = fact.get("temp").getAsString();
                String feelsLike = fact.get("feels_like").getAsString();
                String condition = fact.get("condition").getAsString();
                String windSpeed = fact.get("wind_speed").getAsString();
                String humidity = fact.get("humidity").getAsString();
                String location = loc.substring(0, 1).toUpperCase() + loc.substring(1);

                // Формируем строку с данными о погоде
                String weatherReport = "Погода в " + location +
                        " в " + currentTime +
                        ": Температура " + temperature + " градусов, ощущается как " + feelsLike +
                        " градусов, " + condition + ", скорость ветра " + windSpeed +
                        " м/с, влажность воздуха " + humidity + "%";

                return weatherReport;
            } catch (IOException e) {
                e.printStackTrace();
                return "INVALID";
            }
        } else {
            commandStateManager.setWaiting(chatId, CommandState.WEATHER, true);
            return "INVALID";
        }
    }

    /**
     * Метод проверяет, что полученный JSON-объект из Яндекс Геокодер не пустой
     * */
    private boolean checkYandexGeocoder(JsonObject jsonResponse) {
        JsonArray featureMemberArray = jsonResponse.getAsJsonObject("response")
                .getAsJsonObject("GeoObjectCollection")
                .getAsJsonArray("featureMember");
        return featureMemberArray.size() > 0;
    }

    /**
     * Метод отправляет информацию о погоде в заданном регионе/городе.
     * */
    public String sendWeatherMessage(String chatId, Message message) {

        return getYandexWeather(chatId, message);
    }
}
