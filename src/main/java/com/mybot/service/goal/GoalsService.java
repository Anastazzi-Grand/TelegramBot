package com.mybot.service.goal;

import com.mybot.service.DB.DataBaseConnector;
import com.mybot.service.state_manager.CommandStateManager;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.sql.*;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class GoalsService {
    private CommandStateManager commandStateManager;

    public GoalsService(CommandStateManager commandStateManager) {
        this.commandStateManager = commandStateManager;
    }

    /**
     * Метод для проверки существования БД по id пользователя.
     * */
    public boolean checkBD(String id) {
        boolean tableExists = false;
        try (Connection connection = DataBaseConnector.getUsersDBConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet tables = metaData.getTables(null, null, "goals_" + id, new String[]{"TABLE"})) {
                if (tables.next()) {
                    tableExists = true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Ошибка в соединении с БД");
        }
        return tableExists;
    }

    /**
     * Метод для создания уникальной таблицы в БД для пользователя, если таковой еще нет.
     *
     * @param message Первая записанная цель
     * */
    public void createBD(String id, Message message) {
        String initialGoal = message.getText();
        try (Connection connection = DataBaseConnector.getUsersDBConnection();
             Statement statement = connection.createStatement()) {
            String tableName = "goals_" + id;
            String query = String.format("CREATE TABLE %s (id VARCHAR(255) PRIMARY KEY, goals JSONB)", tableName);
            statement.execute(query);
            JSONObject initialJSON = new JSONObject();
            initialJSON.put("1", initialGoal);
            String initialJSONStr = initialJSON.toString();
            String insertQuery = String.format("INSERT INTO %s (id, goals) VALUES ('%s', '%s')", tableName, id, initialJSONStr);
            statement.execute(insertQuery);
            // Добавляем запись о созданной таблице в общую таблицу пользователей, если необходимо
        } catch (SQLException e) {
            // обработка ошибки создания таблицы
            e.printStackTrace();
            System.out.println("Ошибка в соединении с БД");
        }
    }

    /**
     * Метод для получения всех целей из БД.
     * */
    public String getData(String id) {
        String tableName = "goals_" + id;
        String result = "";
        String selectQuery = String.format("SELECT goals FROM %s", tableName);

        try (Connection connection = DataBaseConnector.getUsersDBConnection();
             Statement statement = connection.createStatement()) {

            ResultSet resultSet = statement.executeQuery(selectQuery);
            if (resultSet.next()) {
                result = resultSet.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Ошибка в соединении с БД");
        }

        // Парсинг JSON и форматирование для вывода
        JSONObject json = new JSONObject(result);

        // Создаем TreeMap и используем его для сортировки ключей по возрастанию
        Map<Integer, String> sortedJson = new TreeMap<>();
        for (String key : json.keySet()) {
            sortedJson.put(Integer.parseInt(key), json.getString(key));
        }

        StringBuilder formattedData = new StringBuilder();
        for (Map.Entry<Integer, String> entry : sortedJson.entrySet()) {
            formattedData.append(entry.getKey()).append(": ").append(entry.getValue());
            formattedData.append("; \n");
        }
        return formattedData.toString();
    }

    /**
     * Метод для добавления цели в БД.
     * */
    public void pushData(String id, Message message) {
        String goal = message.getText();
        if (checkBD(id)) {
            try (Connection connection = DataBaseConnector.getUsersDBConnection();
                 Statement statement = connection.createStatement()) {
                String tableName = "goals_" + id;
                String selectQuery = String.format("SELECT goals FROM %s", tableName);
                ResultSet resultSet = statement.executeQuery(selectQuery);
                if (resultSet.next()) {
                    // Получаем JSON объект с целями
                    String goalsJSON = resultSet.getString("goals");
                    JSONObject goalsObject = new JSONObject(goalsJSON);

                    // Находим последний номер цели
                    int lastGoalNumber = 1;
                    for (String key : goalsObject.keySet()) {
                        int currentNumber = Integer.parseInt(key);
                        if (currentNumber > lastGoalNumber) {
                            lastGoalNumber = currentNumber;
                        }
                    }

                    // Создаем новую цель с номером на 1 больше, чем последний
                    int newGoalNumber = lastGoalNumber + 1;
                    goalsObject.put(String.valueOf(newGoalNumber), goal);

                    // Заменяем JSONObject на TreeMap для сохранения порядка ключей
                    Map<String, String> sortedGoals = new TreeMap<>();
                    for (String key : goalsObject.keySet()) {
                        sortedGoals.put(key, goalsObject.getString(key));
                    }

                    // Обновляем цели в базе данных
                    String updateQuery = String.format("UPDATE %s SET goals='%s'", tableName, new JSONObject(sortedGoals).toString());
                    statement.executeUpdate(updateQuery);
                }
            } catch (SQLException e) {
                // Обработка ошибок при обновлении цели
                e.printStackTrace();
            }
        } else {
            // Если таблицы для пользователя еще не существует, то она создается и сразу в нее записывается первая цель
            createBD(id, message);
        }
    }

    /**
     * Метод для обновления цели в БД по ее номеру.
     *
     * @param id Уникальный номер чата
     * @param messageN Номер цели, которую нужно перезаписать
     * @param messageText Новая цель
     * */
    public void updateData(String id, Message messageN, Message messageText) {
        String text = " ";
        try (Connection connection = DataBaseConnector.getUsersDBConnection();
             Statement statement = connection.createStatement()) {
            String tableName = "goals_" + id;
            String selectQuery = String.format("SELECT goals FROM %s", tableName);
            ResultSet resultSet = statement.executeQuery(selectQuery);

            String number = messageN.getText().substring(0,1);
            if (messageText.getText().length() > 1) text = messageText.getText().substring(2);
            if (resultSet.next()) {
                String goalsJson = resultSet.getString("goals");
                JSONObject jsonObject = new JSONObject(goalsJson);

                jsonObject.put(number, text);
                // Обновляем цели в базе данных
                String updateQuery = String.format("UPDATE %s SET goals='%s'", tableName, jsonObject);
                statement.executeUpdate(updateQuery);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Метод для удаления всех целей из БД.
     * */
    public void deleteData(String id) {
        try (Connection connection = DataBaseConnector.getUsersDBConnection();
             Statement statement = connection.createStatement()) {
            String tableName = "goals_" + id;
            String deleteQuery = String.format("DROP TABLE IF EXISTS %s", tableName);
            statement.executeUpdate(deleteQuery);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Перегруженный метод для удаления цели из БД по ее номеру.
     *
     * @param id уникальный номер чата
     * @param messageN сообщение, содержащее информацию о цели, которую нужно удалить
     */
    public void deleteData(String id, Message messageN) {
        String number = messageN.getText();
        try (Connection connection = DataBaseConnector.getUsersDBConnection();
             Statement statement = connection.createStatement()) {
            String tableName = "goals_" + id;
            String selectQuery = String.format("SELECT goals FROM %s", tableName);
            ResultSet resultSet = statement.executeQuery(selectQuery);

            if (resultSet.next()) {
                String goalsJson = resultSet.getString("goals");
                JSONObject jsonObject = new JSONObject(goalsJson);

                // Удаляем цель из JSON
                jsonObject.remove(number);

                String updateQuery = String.format("UPDATE %s SET goals='%s'", tableName, jsonObject);
                statement.executeUpdate(updateQuery);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Проверка на существование цели с выбранным номером.
     *
     * @param message Сообщение, содержащее информацию о цели, которую нужно редактировать/удалить
     * @param forUpdate true - для команды /update, false - для команды /deleteOne
     * */
    public boolean checkGoal(String id, Message message, boolean forUpdate) {
        String number;
        if (forUpdate) {
            number = message.getText().substring(0, 1);
        } else {
            number = message.getText();
        }

        boolean check = false;
        try (Connection connection = DataBaseConnector.getUsersDBConnection();
             Statement statement = connection.createStatement()) {
            String tableName = "goals_" + id;
            String selectQuery = String.format("SELECT goals FROM %s", tableName);
            ResultSet resultSet = statement.executeQuery(selectQuery);

            if (resultSet.next()) {
                String goalsJson = resultSet.getString("goals");
                JSONObject jsonObject = new JSONObject(goalsJson);

                if (jsonObject.has(number)) {
                    check = true;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return check;
    }

    /**
     * Проверка на то, что файл JSON не пустой.
     * */
    public boolean checkJSON(String id) {
        try (Connection connection = DataBaseConnector.getUsersDBConnection();
             Statement statement = connection.createStatement()) {
            String tableName = "goals_" + id;
            String selectQuery = String.format("SELECT goals FROM %s", tableName);
            ResultSet resultSet = statement.executeQuery(selectQuery);

            if (resultSet.next()) {
                String goalsJson = resultSet.getString("goals");
                JSONObject jsonObject = new JSONObject(goalsJson);
                return jsonObject.length() == 0; // Если JSON пустой, вернуть true
            } else {
                return true; // Если в базе данных нет записей для данного id, вернуть true
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
