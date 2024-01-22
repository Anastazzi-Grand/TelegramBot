package com.mybot.bot;
import com.mybot.service.*;
import com.mybot.service.goal.Goal;
import com.mybot.service.goal.GoalsService;
import com.mybot.service.state_manager.CommandState;
import com.mybot.service.state_manager.CommandStateManager;
import com.mybot.service.weather.Weather;
import com.mybot.service.weather.WeatherService;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.io.IOException;

public class CatBot extends TelegramLongPollingBot {

    static {
        ConnectionService.getConnect();
    }
    private static String botToken = ConnectionService.properties.getProperty("BOT_TOKEN");
    private static String botName = ConnectionService.properties.getProperty("BOT_NAME");

    private CatBotService catBotService = new CatBotService();
    private Goal goal = new Goal();
    private Weather weather = new Weather();

    private RandomCatPhotosService randomCatPhotosService = new RandomCatPhotosService();
    private MemesAndCatsService memesAndCatsService = new MemesAndCatsService();

    private CommandStateManager commandStateManager = new CommandStateManager();
    private GoalsService goalsService = new GoalsService(commandStateManager);

    private WeatherService weatherService = new WeatherService(commandStateManager);

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            handleCallbackQuery(callbackQuery);
        }
        else if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            handleMessage(message);
        }
    }

    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        String chatId = callbackQuery.getMessage().getChatId().toString();
        String callbackData = callbackQuery.getData();
        try {
            if (goalsService.checkBD(chatId)) {
                switch (callbackData) {
                    case "/push":
                        execute(goal.pushCommand(chatId));
                        commandStateManager.nullStateWaiting(chatId);
                        commandStateManager.setWaiting(chatId, CommandState.PUSH,true);
                        break;
                    case "/get":
                        execute(goal.getCommand(chatId));
                        commandStateManager.nullStateWaiting(chatId);
                        break;
                    case "/update":
                        execute(goal.updateCommand(chatId));
                        commandStateManager.nullStateWaiting(chatId);
                        commandStateManager.setWaiting(chatId, CommandState.UPDATE,true);
                        break;
                    case "/delete":
                        execute(catBotService.sendDeleteGoalPanel(chatId));
                        commandStateManager.nullStateWaiting(chatId);
                        break;
                    case "/deleteOne":
                        execute(goal.deleteOneCommand(chatId));
                        commandStateManager.nullStateWaiting(chatId);
                        commandStateManager.setWaiting(chatId, CommandState.DELETE_ONE, true);
                        break;
                    case "/deleteAll":
                        execute(catBotService.sendDeleteAllGoals(chatId));
                        commandStateManager.nullStateWaiting(chatId);
                        break;
                    case "/yes":
                        execute(goal.yesCommand(chatId));
                        commandStateManager.nullStateWaiting(chatId);
                        break;
                    case "/no":
                        execute(goal.noCommand(chatId));
                        commandStateManager.nullStateWaiting(chatId);
                        break;
                }
            } else if (!callbackData.equals("/push")) {
                execute(goal.noDataCommand(chatId));
            } else {
                execute(goal.firstPushCommand(chatId));
                commandStateManager.setWaiting(chatId, CommandState.PUSH, true);
            }
        } catch(TelegramApiException e){
            throw new RuntimeException(e);
        }
    }

    private void handleMessage(Message message) {
        String chatId = message.getChatId().toString();
        String text = message.getText();
        try {
            SendPhoto sendPhoto = null;
            switch (text) {
                case "/start":
                    execute(catBotService.sendStartMessage(chatId));
                    commandStateManager.nullStateWaiting(chatId);
                    break;
                case "Показать котят":
                    sendPhoto = randomCatPhotosService.sendRandomCatPhoto(chatId);
                    commandStateManager.nullStateWaiting(chatId);
                    break;
                case "Мемы и коты":
                    sendPhoto = memesAndCatsService.sendPhotoFromBD(chatId);
                    commandStateManager.nullStateWaiting(chatId);
                    break;
                case "/goal":
                    execute(catBotService.sendGoalsPanel(chatId));
                    commandStateManager.nullStateWaiting(chatId);
                    break;
                case "/weather":
                    execute(weather.introduceMessageCommand(chatId));
                    commandStateManager.nullStateWaiting(chatId);
                    commandStateManager.setWaiting(chatId, CommandState.WEATHER, true);
                    break;
                default:
                    if (commandStateManager.isWaiting(chatId, CommandState.WEATHER)) {
                        String s = weatherService.sendWeatherMessage(chatId, message);
                        if (s.equals("INVALID")) {
                            execute(weather.stateWeatherWarningMessage(chatId));
                            commandStateManager.setWaiting(chatId, CommandState.WEATHER, true);
                        } else {
                            execute(new SendMessage(chatId, s));
                            commandStateManager.setWaiting(chatId, CommandState.WEATHER, false);
                        }
                    } else if (commandStateManager.isWaiting(chatId, CommandState.PUSH)) {
                        execute(goal.statePushMessageCommand(chatId, message));
                        commandStateManager.setWaiting(chatId, CommandState.PUSH,false);
                    } else if (commandStateManager.isWaiting(chatId, CommandState.UPDATE)){
                        if (goalsService.checkGoal(chatId, message, true)) {
                            execute(goal.stateUpdateMessageGood(chatId, message));
                            commandStateManager.setWaiting(chatId, CommandState.UPDATE, false);
                        } else {
                            execute(goal.stateUpdateMessageBad(chatId));
                            commandStateManager.setWaiting(chatId, CommandState.UPDATE, true);
                        }
                    } else if (commandStateManager.isWaiting(chatId, CommandState.DELETE_ONE)) {
                        if (goalsService.checkGoal(chatId, message, false)) {
                            execute(goal.stateDeleteOneMessageGood(chatId, message));
                            commandStateManager.setWaiting(chatId, CommandState.DELETE_ONE, false);
                        } else {
                            commandStateManager.setWaiting(chatId, CommandState.DELETE_ONE, true);
                            execute(goal.stateDeleteOneMessageBad(chatId));
                        }
                    } else {
                        // Обработка неизвестных команд
                        execute(new SendMessage(chatId, "Неизвестная команда"));
                    }
                    break;
            }

            if (sendPhoto != null) {
                execute(sendPhoto);
            }
        } catch (IOException | TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
