package telegram;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
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

public class CatBot extends TelegramLongPollingBot {
    static Properties properties = new Properties();
    static {
        try (InputStream inputStream = CatBot.class.getClassLoader().getResourceAsStream("config.properties")) {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static String botToken = properties.getProperty("BOT_TOKEN");
    private static String botName = properties.getProperty("BOT_NAME");

    private static String CAT_API_KEY = properties.getProperty("API_KEY");
    private static final String CAT_API_URL = "https://api.thecatapi.com/v1/images/search";


    // Storage storage;
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

            if (text.equals("/start")) {
                sendStartMessage(chatId);
            } else if (text.equals("Показать котят")) {
                sendRandomCatPhoto(chatId);
            }
        }
    }

    private void sendStartMessage(String chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Нажмите кнопку, чтобы увидеть фото котика");

        // Создаем объект клавиатуры
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true); // РџРѕРґРіРѕРЅСЏРµРј СЂР°Р·РјРµСЂ РєР»Р°РІРёР°С‚СѓСЂС‹

        // Создаем ряд кнопок
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow keyboardRow = new KeyboardRow();
        keyboardRows.add(keyboardRow);

        // Создаем кнопку "Показать котят" и добавляем ее в ряд
        KeyboardButton showCatsButton = new KeyboardButton();
        showCatsButton.setText("Показать котят");
        keyboardRow.add(showCatsButton);

        // Устанавливаем клавиатуру в сообщение
        replyKeyboardMarkup.setKeyboard(keyboardRows);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendRandomCatPhoto(String chatId) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(CAT_API_URL)
                .addHeader("x-api-key", CAT_API_KEY)
                .get()
                .build();

        try {
            Response response = client.newCall(request).execute();
            String jsonData = response.body().string();
            JsonParser parser = new JsonParser();
            JsonObject jsonObject = parser.parse(jsonData).getAsJsonArray().get(0).getAsJsonObject();
            String imageUrl = jsonObject.get("url").getAsString();
            Request imageRequest = new Request.Builder().url(imageUrl).build();
            Response imageResponse = client.newCall(imageRequest).execute();
            InputStream inputStream = imageResponse.body().byteStream();
            File tempFile = File.createTempFile("temp", ".jpg");
            Files.copy(inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            SendPhoto sendPhoto = new SendPhoto();
            sendPhoto.setChatId(chatId);
            sendPhoto.setPhoto(new InputFile(tempFile));

            execute(sendPhoto); // Отправка фото пользователю

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
