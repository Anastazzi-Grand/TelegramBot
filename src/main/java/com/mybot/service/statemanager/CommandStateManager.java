package com.mybot.service.statemanager;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Класс для управления состоянием ожидания ввода региона/города для погоды.
 * */
public class CommandStateManager implements WaitingStateManager {

    Map<String, Map<CommandState, Boolean>> waitingMap = new HashMap<>();

    @Override
    public void setWaiting(String chatId, CommandState state, boolean waiting) {
        waitingMap.computeIfAbsent(chatId, k -> new HashMap<>()).put(state, waiting);
    }

    @Override
    public boolean isWaiting(String chatId, CommandState state) {
        return waitingMap.getOrDefault(chatId, Collections.emptyMap()).getOrDefault(state, false);
    }

    @Override
    public void nullStateWaiting(String chatId) {
        for (CommandState commandState : CommandState.values()) {
            setWaiting(chatId, commandState, false);
        }
    }
}
