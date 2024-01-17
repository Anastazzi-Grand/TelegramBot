package com.mybot.service.statemanager;


/**
 * Интерфейс для управления состоянием ожидания ввода от пользователя.
 * */
public interface WaitingStateManager {

    /**
     * Метод для установки состояния ожидания ввода.
     *
     * @param chatId Уникальный номер чата
     * @param state Тип команды для ожидания сообщения
     * @param waiting Состояние ожидания сообщения
     * */
    void setWaiting(String chatId, CommandState state, boolean waiting);

    /**
     * Метод возвращает boolean значения проверки состояния ввода.
     *
     * @param chatId Уникальный номер чата
     * */
    boolean isWaiting(String chatId, CommandState state);

    /**
     * Обнуление состояния ожидания для всех команд.
     *
     * @param chatId Уникальный номер чата
     * */
    void nullStateWaiting(String chatId);
}
