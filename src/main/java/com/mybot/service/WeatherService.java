package com.mybot.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mybot.service.statemanager.CommandState;
import com.mybot.service.statemanager.CommandStateManager;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

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
     * Метод для отправки вводного сообщения для команды /weather.
     * Возвращает сообщение типа SendMessage.
     *
     * @param chatId
     * */
    public SendMessage sendIntroduceMessage(String chatId) {
        SendMessage sendMessage = new SendMessage(chatId, "Введите название региона/города, в котором хотите узнать погоду на сегодня (например, Московская область).");
        commandStateManager.setWaiting(chatId, CommandState.WEATHER, true); // Устанавливаем состояние ожидания ввода
        return sendMessage;
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
        System.out.println("ОТРАБОТАЛ МЕТОД processUserMessage");

        return location;
    }

    /**
     * Метод, который получает широту и долготу отправленного в сообщении региона/города через Яндекс API Геокодер.
     * */
    private String getYandexGeocoder(String chatId, Message message) {
        try {
            String location = processUserMessage(chatId, message).toLowerCase();
            System.out.println("ГОРОД В МЕТОДЕ getYandexGeocoder " + location);
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
                    return coordinates + " " + location;
                } else {
                    System.out.println("СРАБОТАЛА ОШИБКА");
                    return "INVALID";
                }
            }
            System.out.println("ОТРАБОТАЛ МЕТОД getYandexGeocoder В БЛОКЕ TRY");
        } catch (IOException e) {
            e.printStackTrace();
            return "INVALID";
        }
       // System.out.println("ОТРАБОТАЛ МЕТОД getYandexGeocoder");
        return "INVALID";
    }

    /**
     * Метод отправляет координаты региона в Яндекс Погоду для получения сведений о погоде.
     * */
    private String getYandexWeather(String chatId, Message message) {
        String coordinates = getYandexGeocoder(chatId, message);

        if (!coordinates.equals("INVALID")) {
            String apiUrl = "https://api.weather.yandex.ru/v2/forecast?lat=" + coordinates.split(" ")[1] + "&lon=" + coordinates.split(" ")[0] + "&lang=ru_RU";
            String loc = coordinates.split(" ")[2];
            System.out.println("ГОРОД В МЕТОДЕ getYandexWeather " + loc);

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

                System.out.println("ОТРАБОТАЛ МЕТОД getYandexWeather В БЛОКЕ TRY");
                return weatherReport;
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("ОТРАБОТАЛ МЕТОД getYandexWeather В БЛОКЕ CATCH");
                return "INVALID";
            }
        } else {
            System.out.println("ОТРАБОТАЛ БЛОК else В МЕТОДЕ getYandexWeather");
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
     * Метод для отправки предупреждающего сообщения в случае неверно введенного названия города/региона.
     * Возвращает сообщение типа SendMessage.
     *
     * @param chatId
     * */
    public SendMessage sendWarningMessage(String chatId) {
        SendMessage sendMessage = new SendMessage(chatId, "Введено неверное название города/региона. Попробуйте отправить сообщение снова (например, Москва).");
        commandStateManager.setWaiting(chatId, CommandState.WEATHER, true); // Устанавливаем состояние ожидания ввода
        System.out.println("ОТРАБОТАЛ МЕТОД sendWarningMessage");
        return sendMessage;
    }

    /**
     * Метод отправляет информацию о погоде в заданном регионе/городе.
     * */
    public String sendWeatherMessage(String chatId, Message message) {
        String t = getYandexWeather(chatId, message);
        System.out.println("ОТРАБОТАЛ МЕТОД sendWeatherMessage");

        return t;
    }
}
