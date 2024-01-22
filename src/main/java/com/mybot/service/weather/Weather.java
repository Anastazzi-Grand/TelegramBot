package com.mybot.service.weather;

import com.mybot.service.state_manager.CommandState;
import com.mybot.service.state_manager.CommandStateManager;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

public class Weather {
    private CommandStateManager commandStateManager = new CommandStateManager();
    private WeatherService weatherService = new WeatherService(commandStateManager);

    /**
     * Метод для отправки вводного сообщения для команды /weather.
     * Возвращает сообщение типа SendMessage.
     *
     * */
    public SendMessage introduceMessageCommand(String chatId) {
        return new SendMessage(chatId, "Введите название региона/города, в котором хотите узнать погоду на сегодня (например, Московская область).");
    }

    /**
     * Метод отправляет предупреждение при неверном сообщения от пользователя после команды /weather.
     * */
    public SendMessage stateWeatherWarningMessage(String chatId) {
        return new SendMessage(chatId, "Введено неверное название города/региона. Попробуйте отправить сообщение снова (например, Москва).");
    }
}
