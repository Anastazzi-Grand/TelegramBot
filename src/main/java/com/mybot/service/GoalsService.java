package com.mybot.service;

import com.mybot.util.DataBaseConnector;

import java.sql.Connection;

public class GoalsService {
    DataBaseConnector databaseConnector;

    // для просмотра, редактирования, удаления, добавления целей нужно сделать 4 кнопки типа InlineKeyboard
    // вид таблицы: id - ник, username - имя, goals - цели (JSONB формат)
    // проверка в таблицах по id: если такое id есть, работаем с json файлом. Если нет, создаем новую таблицу.
    // возможность смотреть цели (get - read), удалять цели (remove - delete), редактировать (update), добавлять (push - create)
    // при нажатии на кнопки редактировать и удалить будет срабатывать команда read для просмотра целей для изменения/удаления
    // в кнопке удалить предусмотреть возможность для удаления всех целей
    // пользователь может работать только со своими данными
    // если бд пустая или не существует, при выборе команд должно выводиться сообщение: У вас еще нет записанных целей. Вы можете их записать с помощью кнопки "Создать"

    //работа команды /goal: после ее отправки отображаются 4 inline кнопки (crud)

    public void handleCallbackData(String callbackData, String chatId) {
        switch (callbackData) {
            case "/push":
                System.out.println("Отработала команда /push");
                // Логика для добавления цели
                break;
            case "/get":
                System.out.println("Отработала команда /get");
                // Логика для просмотра цели
                break;
            case "/update":
                System.out.println("Отработала команда /update");
                // Логика для редактирования цели
                break;
            case "/delete":
                System.out.println("Отработала команда /delete");
                // Логика для удаления цели
                break;
        }
    }
}
