package com.mybot.service.statemanager;

import java.util.HashMap;
import java.util.Map;

/**
 * Класс для управления состоянием ожидания ввода цели для callback data.
 * */
public class GoalStateManager implements WaitingStateManager {

    private Map<String, Boolean> waitingForPushMap = new HashMap<>();
    private Map<String, Boolean> waitingForEditMap = new HashMap<>();
    private Map<String, Boolean> waitingForDeleteMap = new HashMap<>();

    @Override
    public void setWaiting(String chatId, boolean waiting) {
        waitingForPushMap.put(chatId, waiting);
    }
    @Override
    public boolean isWaiting(String chatId) {
        return waitingForPushMap.getOrDefault(chatId, false);
    }

    public void setWaitingEdit(String chatId, boolean waiting) {
        waitingForEditMap.put(chatId, waiting);
    }

    public boolean isWaitingEdit(String chatId) {
        return waitingForEditMap.getOrDefault(chatId, false);
    }

    public void setWaitingDelete(String chatId, boolean waiting) {
        waitingForDeleteMap.put(chatId, waiting);
    }

    public boolean isWaitingDelete(String chatId) {
        return waitingForDeleteMap.getOrDefault(chatId, false);
    }


}
