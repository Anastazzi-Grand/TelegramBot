package com.mybot.service;

import java.util.HashMap;
import java.util.Map;

/**
 * Класс для управления состоянием ожидания ввода региона/города для погоды.
 * */
public class WeatherStateManager {
    private Map<String, Boolean> waitingForLocationMap = new HashMap<>();

    /**
     * Метод для установки состояния ожидания ввода.
     *
     * @param chatId
     * @param waiting
     * */
    public void setWaitingForLocation(String chatId, boolean waiting) {
        waitingForLocationMap.put(chatId, waiting);
    }

    /**
     * Метод возвращает boolean значения проверки состояния ввода.
     *
     * @param chatId
     * */
    public boolean isWaitingForLocation(String chatId) {
        return waitingForLocationMap.getOrDefault(chatId, false);
    }
}
