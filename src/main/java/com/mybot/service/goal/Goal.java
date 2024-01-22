package com.mybot.service.goal;

import com.mybot.service.state_manager.CommandState;
import com.mybot.service.state_manager.CommandStateManager;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

public class Goal {
    private CommandStateManager commandStateManager = new CommandStateManager();
    private GoalsService goalsService = new GoalsService(commandStateManager);

    /**
     * Отправление сообщения при выборе кнопки "Добавить".
     * */
    public SendMessage pushCommand(String chatId) {

        return new SendMessage(chatId, "Отправьте сюда цель, которую хотите сохранить.");
    }

    /**
     * Отправление сообщения при выборе кнопки "Добавить", если таблицы для пользователя еще не существует.
     * */
    public SendMessage firstPushCommand(String chatId) {
        return new SendMessage(chatId, "Отправьте сюда цель, которую хотите сохранить.");
    }

    /**
     * Отправление сообщения при выборе кнопки "Просмотреть".
     * */
    public SendMessage getCommand(String chatId) {
        return new SendMessage(chatId, goalsService.getData(chatId));
    }

    /**
     * Отправление сообщения при выборе кнопки "Редактировать".
     * */
    public SendMessage updateCommand(String chatId) {
        String text = "Отправьте номер цели и ее новое описание, который хотите обновить в формате [номер цели] [формулировка цели]. Например, \"4 Дочитать книгу\". Можно изменить только те цели, которые есть в списке: \n";

        return new SendMessage(chatId, text + goalsService.getData(chatId));
    }

    /**
     * Отправление сообщения при выборе кнопки "Удалить 1 цель".
     * */
    public SendMessage deleteOneCommand(String chatId) {
        String text = "Отправьте номер цели, который хотите удалить (например, \"4\". Можно удалить только те цели, которые есть в списке: \n";

        return new SendMessage(chatId, text + goalsService.getData(chatId));
    }

    /**
     * Отправление сообщения при выборе кнопки "Да" после выбора "Удалить все цели".
     * */
    public SendMessage yesCommand(String chatId) {
        goalsService.deleteData(chatId);

        return new SendMessage(chatId, "Все данные удалены.");
    }

    /**
     * Отправление сообщения при выборе кнопки "Нет" после выбора "Удалить все цели".
     * */
    public SendMessage noCommand(String chatId) {
        return new SendMessage(chatId, "Все данные сохранены.");
    }

    /**
     * Отправление сообщения после выбора кнопок "Редактировать", "Удалить" и "Просмотреть", если у пользователя нет таблицы в БД.
     * */
    public SendMessage noDataCommand(String chatId) {
        return new SendMessage(chatId, "У вас еще нет записанных целей. Вы можете внести их с помощью кнопки \"Добавить\".");
    }

    /**
     * Отправление сообщения при получении ответа от пользователя после выбора команды "Добавить".
     * */
    public SendMessage statePushMessageCommand(String chatId, Message message) {
        goalsService.pushData(chatId, message);

        return new SendMessage(chatId, "Ваша цель успешно записана. Можете просмотреть свои цели, нажав на кнопку \"Просмотреть\".");
    }

    /**
     * Отправление сообщения при получении правильного ответа от пользователя после выбора команды "Редактировать".
     * */
    public SendMessage stateUpdateMessageGood(String chatId, Message message) {
        goalsService.updateData(chatId, message, message);

        return new SendMessage(chatId, "Цель успешно изменена! Можете убедиться в этом, нажав на кнопку \"Просмотреть\"");
    }

    /**
     * Отправление сообщения при получении неправильного ответа от пользователя после выбора команды "Редактировать".
     * */
    public SendMessage stateUpdateMessageBad(String chatId) {
        String t = "Цели с таким номером нет. Выберите цель из списка ниже и перезапишите ее в формате [номер цели] [формулировка цели]. Например, \"4 Дочитать книгу\". Попробуйте снова, исходя из вашего списка с целями: \n";

        return new SendMessage(chatId, t + goalsService.getData(chatId));
    }

    /**
     * Отправление сообщения при получении правильного ответа от пользователя после выбора команды "Удалить 1 цель".
     * */
    public SendMessage stateDeleteOneMessageGood(String chatId, Message message) {
        goalsService.deleteData(chatId, message);
        if (goalsService.checkJSON(chatId)) {
            goalsService.deleteData(chatId);
        }

        return new SendMessage(chatId, "Цель успешно удалена! Можете убедиться в этом, нажав на кнопку \"Просмотреть\"");
    }

    /**
     * Отправление сообщения при получении неправильного ответа от пользователя после выбора команды "Удалить 1 цель".
     * */
    public SendMessage stateDeleteOneMessageBad(String chatId) {
        String t = "Цели с таким номером нет. Выберите цель из списка ниже для ее удаления (например, \"4\"). Попробуйте выбрать снова, исходя из вашего списка с целями: \n";

        return new SendMessage(chatId, t + goalsService.getData(chatId));
    }
}
