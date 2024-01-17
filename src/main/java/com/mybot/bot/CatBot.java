package com.mybot.bot;
import com.mybot.service.*;
import com.mybot.service.statemanager.CommandState;
import com.mybot.service.statemanager.CommandStateManager;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс, определяющий внешний вид бота.
 * */
public class CatBot extends TelegramLongPollingBot {

    /**
     * Соединение с БД
     * */
    static {
        ConnectionService.getConnect();
    }
    private static String botToken = ConnectionService.properties.getProperty("BOT_TOKEN");
    private static String botName = ConnectionService.properties.getProperty("BOT_NAME");

    private RandomCatPhotosService randomCatPhotosService = new RandomCatPhotosService();
    private MemesAndCatsService memesAndCatsService = new MemesAndCatsService();

    private CommandStateManager commandStateManager = new CommandStateManager();
    private WeatherService weatherService = new WeatherService(commandStateManager);
    private GoalsService goalsService = new GoalsService(commandStateManager);

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
            String chatId = callbackQuery.getMessage().getChatId().toString();
            String callbackData = callbackQuery.getData();
            try {
                if (goalsService.checkBD(chatId)) {
                    switch (callbackData) {
                        case "/push":
                            commandStateManager.nullStateWaiting(chatId);
                            commandStateManager.setWaiting(chatId, CommandState.PUSH,true);
                            execute(new SendMessage(chatId, "Отправьте сюда цель, которую хотите сохранить."));
                            break;
                        case "/get":
                            execute(new SendMessage(chatId, goalsService.getData(chatId)));
                            break;
                        case "/update":
                            commandStateManager.nullStateWaiting(chatId);
                            execute(new SendMessage(chatId, "Отправьте номер цели и ее новое описание, который хотите обновить в формате [номер цели] [формулировка цели]. Например, \"4 Дочитать книгу\". Можно изменить только те цели, которые есть в списке:"));
                            execute(new SendMessage(chatId, goalsService.getData(chatId)));
                            commandStateManager.setWaiting(chatId, CommandState.UPDATE,true);
                            break;
                        case "/delete":
                            execute(sendDeleteGoalPanel(chatId));
                            break;
                        case "/deleteOne":
                            commandStateManager.nullStateWaiting(chatId);
                            execute(new SendMessage(chatId, "Отправьте номер цели, который хотите удалить (например, \"4\". Можно удалить только те цели, которые есть в списке:"));
                            execute(new SendMessage(chatId, goalsService.getData(chatId)));
                            commandStateManager.setWaiting(chatId, CommandState.DELETE_ONE, true);
                            break;
                        case "/deleteAll":
                            execute(sendDeleteAllGoals(chatId));
                            break;
                        case "/yes":
                            goalsService.deleteData(chatId);
                            execute(new SendMessage(chatId, "Все данные удалены."));
                            break;
                        case "/no":
                            execute(new SendMessage(chatId, "Все данные сохранены."));
                            break;
                    }
                } else if (!callbackData.equals("/push")) {
                    execute(new SendMessage(chatId, "У вас еще нет записанных целей. Вы можете внести их с помощью кнопки \"Добавить\"."));
                } else {
                    commandStateManager.setWaiting(chatId, CommandState.PUSH, true);
                    execute(new SendMessage(chatId, "Отправьте сюда цель, которую хотите сохранить."));
                }
            } catch(TelegramApiException e){
                throw new RuntimeException(e);
            }
        }
        else if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            String chatId = message.getChatId().toString();
            String text = message.getText();
            try {
                SendPhoto sendPhoto = null;
                switch (text) {
                    case "/start":
                        sendStartMessage(chatId);
                        break;
                    case "Показать котят":
                        sendPhoto = randomCatPhotosService.sendRandomCatPhoto(chatId);
                        break;
                    case "Мемы и коты":
                        sendPhoto = memesAndCatsService.sendPhotoFromBD(chatId);
                        break;
                    case "/goal":
                        execute(sendGoalsPanel(chatId));
                        break;
                    case "/weather":
                        commandStateManager.nullStateWaiting(chatId);
                        execute(weatherService.sendIntroduceMessage(chatId));
                        break;
                    default:
                        if (commandStateManager.isWaiting(chatId, CommandState.WEATHER)) {
                            String s = weatherService.sendWeatherMessage(chatId, message);
                            if (s.equals("INVALID")) {
                                execute(weatherService.sendWarningMessage(chatId));
                            } else { // Обработка успешно полученных координат
                                execute(new SendMessage(chatId, s));
                                commandStateManager.setWaiting(chatId, CommandState.WEATHER, false); // Устанавливаем состояние ожидания ввода в false
                            }
                        } else if (commandStateManager.isWaiting(chatId, CommandState.PUSH)) {
                            // Обработка ожидания ввода цели после нажатия на кнопку "Добавить" (/push) команды /goal
                            goalsService.pushData(chatId, message); // Сохранить цель в базу данных
                            execute(new SendMessage(chatId, "Ваша цель успешно записана. Можете просмотреть свои цели, нажав на кнопку \"Просмотреть\"."));
                            commandStateManager.setWaiting(chatId, CommandState.PUSH,false); // Установить состояние ожидания ввода цели в false
                        } else if (commandStateManager.isWaiting(chatId, CommandState.UPDATE)){
                            if (goalsService.checkGoal(chatId, message)) {
                                goalsService.updateData(chatId, message, message);
                                execute(new SendMessage(chatId, "Цель успешно изменена! Можете убедиться в этом, нажав на кнопку \"Просмотреть\""));
                                commandStateManager.setWaiting(chatId, CommandState.UPDATE, false);
                            } else {
                                execute(new SendMessage(chatId, "Цели с таким номером нет. Выберите цель из списка ниже и перезапишите ее в формате [номер цели] [формулировка цели]. Например, \"4 Дочитать книгу\". Попробуйте снова, исходя из вашего списка с целями:"));
                                execute(new SendMessage(chatId, goalsService.getData(chatId)));
                                commandStateManager.setWaiting(chatId, CommandState.UPDATE, true);
                            }
                        } else if (commandStateManager.isWaiting(chatId, CommandState.DELETE_ONE)) {
                            if (goalsService.checkGoalForDelete(chatId, message)) {
                                goalsService.deleteData(chatId, message);
                                execute(new SendMessage(chatId, "Цель успешно удалена! Можете убедиться в этом, нажав на кнопку \"Просмотреть\""));
                                commandStateManager.setWaiting(chatId, CommandState.DELETE_ONE, false);
                                if (goalsService.checkJSON(chatId)) {
                                    goalsService.deleteData(chatId);
                                }
                            } else {
                                execute(new SendMessage(chatId, "Цели с таким номером нет. Выберите цель из списка ниже для ее удаления (например, \"4\"). Попробуйте снова, исходя из вашего списка с целями:"));
                                execute(new SendMessage(chatId, goalsService.getData(chatId)));
                                commandStateManager.setWaiting(chatId, CommandState.DELETE_ONE,true);
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

    /**
     * Метод для отправки изначального сообщения.
     * */
    private void sendStartMessage(String chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Нажмите кнопку, чтобы увидеть фото котика");

        // Создаем объект клавиатуры
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);

        // Создаем ряд для каждой кнопки
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow keyboardRow = new KeyboardRow();

        // Создаем кнопку "Показать котят" и добавляем ее в ряд
        KeyboardButton showCatsButton = new KeyboardButton();
        showCatsButton.setText("Показать котят");
        keyboardRow.add(showCatsButton);
        keyboardRows.add(keyboardRow);

        keyboardRow = new KeyboardRow();
        KeyboardButton showMemeAndCatsButton = new KeyboardButton();
        showMemeAndCatsButton.setText("Мемы и коты");
        keyboardRow.add(showMemeAndCatsButton);
        keyboardRows.add(keyboardRow);

        // Устанавливаем клавиатуру в сообщение
        replyKeyboardMarkup.setKeyboard(keyboardRows);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    /**
     * Метод для отображения кнопок при выборе команды /goal
     * */
    private SendMessage sendGoalsPanel(String chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Здесь вы можете хранить свои цели или планы на будущее. Записывайте по одной цели за раз. Отредактируйте их с помощью команд ниже.");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        inlineKeyboardButton1.setText("Добавить");
        inlineKeyboardButton1.setCallbackData("/push");
        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
        inlineKeyboardButton2.setText("Просмотреть");
        inlineKeyboardButton2.setCallbackData("/get");
        rowInline1.add(inlineKeyboardButton1);
        rowInline1.add(inlineKeyboardButton2);

        List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
        InlineKeyboardButton inlineKeyboardButton3 = new InlineKeyboardButton();
        inlineKeyboardButton3.setText("Редактировать");
        inlineKeyboardButton3.setCallbackData("/update");
        InlineKeyboardButton inlineKeyboardButton4 = new InlineKeyboardButton();
        inlineKeyboardButton4.setText("Удалить");
        inlineKeyboardButton4.setCallbackData("/delete");
        rowInline2.add(inlineKeyboardButton3);
        rowInline2.add(inlineKeyboardButton4);

        rowsInline.add(rowInline1);
        rowsInline.add(rowInline2);

        markup.setKeyboard(rowsInline);
        sendMessage.setReplyMarkup(markup);

        return sendMessage;
    }

    /**
     * Метод для отображения кнопок при выборе команды /delete
     * */
    private SendMessage sendDeleteGoalPanel(String chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Вы можете удалить либо одну цель, либо все сразу:");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        inlineKeyboardButton1.setText("Удалить 1 цель");
        inlineKeyboardButton1.setCallbackData("/deleteOne");
        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
        inlineKeyboardButton2.setText("Удалить все цели");
        inlineKeyboardButton2.setCallbackData("/deleteAll");
        rowInline1.add(inlineKeyboardButton1);
        rowInline1.add(inlineKeyboardButton2);

        rowsInline.add(rowInline1);

        markup.setKeyboard(rowsInline);
        sendMessage.setReplyMarkup(markup);

        return sendMessage;
    }

    /**
     * Метод для отображения кнопок при выборе команды /deleteAll
     * */
    private SendMessage sendDeleteAllGoals(String chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Вы уверены, что хотите удалить все цели?");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
        InlineKeyboardButton inlineKeyboardButton1 = new InlineKeyboardButton();
        inlineKeyboardButton1.setText("Да");
        inlineKeyboardButton1.setCallbackData("/yes");
        InlineKeyboardButton inlineKeyboardButton2 = new InlineKeyboardButton();
        inlineKeyboardButton2.setText("Нет");
        inlineKeyboardButton2.setCallbackData("/no");
        rowInline1.add(inlineKeyboardButton1);
        rowInline1.add(inlineKeyboardButton2);

        rowsInline.add(rowInline1);

        markup.setKeyboard(rowsInline);
        sendMessage.setReplyMarkup(markup);

        return sendMessage;
    }

}
