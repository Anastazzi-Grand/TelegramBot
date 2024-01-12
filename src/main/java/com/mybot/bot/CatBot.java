package com.mybot.bot;
import com.mybot.service.*;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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

    private WeatherStateManager weatherStateManager = new WeatherStateManager();
    private WeatherService weatherService = new WeatherService(weatherStateManager);

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
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            String chatId = message.getChatId().toString();
            String text = message.getText();

            // Вместо проверки text.equals("/start") можно обработать все сообщения
            try {
                SendPhoto sendPhoto = null;
                switch (text) {
                    case "/start":
                        sendStartMessage(chatId);
                        break;
                    case "Показать котят":
                        sendPhoto = randomCatPhotosService.sendRandomCatPhoto(message.getChatId().toString());
                        break;
                    case "Мемы и коты":
                        sendPhoto = memesAndCatsService.sendPhotoFromBD(message.getChatId().toString());
                        break;
                    case "/weather":
                        System.out.println("WEATHER PRINTED");
                        execute(weatherService.sendIntroduceMessage(chatId));
                        break;
                    default:
                        if (weatherStateManager.isWaitingForLocation(chatId)) {
                            String s = weatherService.sendWeatherMessage(chatId, message);
                            System.out.println("CATBOT onUpdateReceived s = " + s);
                            if (s.equals("INVALID")) {
                                execute(weatherService.sendWarningMessage(chatId));
                            } else { // Обработка успешно полученных координат
                                execute(new SendMessage(chatId, s));
                                System.out.println("ПОГОДА ИЗВЕСТНА КОНЕЦ ВЫПОЛНЕНИЯ КОМАНДЫ");
                                weatherStateManager.setWaitingForLocation(chatId, false); // Устанавливаем состояние ожидания ввода в false
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

}
