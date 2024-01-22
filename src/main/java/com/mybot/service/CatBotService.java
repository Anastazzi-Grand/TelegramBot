package com.mybot.service;

import com.mybot.service.state_manager.CommandStateManager;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

public class CatBotService {
    CommandStateManager commandStateManager = new CommandStateManager();
    /**
     * Метод для отправки первичного сообщения + меню с кнопками.
     * */
    public SendMessage sendStartMessage(String chatId) {
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

        return sendMessage;
    }

    /**
     * Метод для отображения кнопок при выборе команды /goal
     * */
    public SendMessage sendGoalsPanel(String chatId) {
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
    public SendMessage sendDeleteGoalPanel(String chatId) {
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
    public SendMessage sendDeleteAllGoals(String chatId) {
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
