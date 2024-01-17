package com.mybot.service.statemanager;

import java.util.HashMap;
import java.util.Map;

/**
 * Класс для управления состоянием ожидания ввода региона/города для погоды.
 * */
public class WeatherStateManager implements WaitingStateManager {
    private Map<String, Boolean> waitingForLocationMap = new HashMap<>();
    @Override
    public void setWaiting(String chatId, boolean waiting) {
        waitingForLocationMap.put(chatId, waiting);
    }

    @Override
    public boolean isWaiting(String chatId) {
        return waitingForLocationMap.getOrDefault(chatId, false);
    }
}
