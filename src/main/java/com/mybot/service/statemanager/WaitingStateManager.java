package com.mybot.service.statemanager;


/**
 * Интерфейс для управления состоянием ожидания ввода от пользователя.
 * */
public interface WaitingStateManager {

    /**
     * Метод для установки состояния ожидания ввода.
     *
     * @param chatId
     * @param waiting
     * */
    void setWaiting(String chatId, boolean waiting);

    /**
     * Метод возвращает boolean значения проверки состояния ввода.
     *
     * @param chatId
     * */
    boolean isWaiting(String chatId);
}
