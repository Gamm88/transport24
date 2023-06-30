package ru.transport24.bot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.transport24.bot.exception.ValidatorExceptions;
import ru.transport24.bot.model.Card;
import ru.transport24.bot.model.CardType;
import ru.transport24.bot.model.User;
import ru.transport24.bot.repository.CardRepository;
import ru.transport24.bot.repository.UserRepository;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardService {
    final UserService userService;
    final UserRepository userRepository;
    final CardRepository cardRepository;

    // Добавление карты.
    String addCard(Long chatId, String text, Update update) {
        // Находим пользователя.
        User user = userService.getUser(chatId);
        // Если пользователь не зарегистрирован, регистрируем его.
        if (user == null) {
            user = userService.addUser(update);
        }
        // Если пользователь уже добавил 3 карты.
        if (user.getCards().size() > 2) {
            return "Каждый пользователь может контролировать баланс максимум трёх карт, " +
                    "удалите ранее добавленную карту!";
        }
        // Получаем номер карты из сообщения (удаляем все кроме цифр).
        String cardNumber = text.replaceAll("\\D+", "");
        // Проверяем, может карта уже добавлена.
        for (Card card : user.getCards()) {
            if (card.getCardNumber().equals(cardNumber)) {
                return "Карта №" + cardNumber + " уже добавлена!";
            }
        }
        // Создаём карту.
        Card card = getCardWithBalance(cardNumber);
        // Связываем карту с пользователем.
        user.addCard(card);
        // Обновляем пользователя в БД.
        userRepository.save(user);
        log.info("Добавлена карта: " + card);
        return card.getCardType().getInRussian() + " №" + card.getCardNumber() + " добавлена!";
    }

    // Удаление карты.
    String deleteCard(Long chatId, String text) {
        // Находим пользователя.
        User user = userService.getUser(chatId);
        // Если пользователь не зарегистрирован.
        if (user == null) {
            return "У вас нет добавленных карт!";
        }
        // Получаем номер карты из сообщения (удаляем все кроме цифр).
        String cardNumber = text.replaceAll("\\D+", "");
        // Ищем карту у пользователя.
        Card card = cardRepository.findTopByUserIdAndCardNumber(chatId, cardNumber);
        // Если у пользователя нет карты с таким номером.
        if (card == null) {
            return "Карта с номером " + cardNumber + " не найдена!";
        }
        // Удаляем карту у пользователя.
        user.removeCard(card);
        // Обновляем пользователя в БД.
        userRepository.save(user);
        log.info("Удалена карта: " + card);
        return card.getCardType().getInRussian() + " №" + card.getCardNumber() + " удалена!";
    }

    // Список карт пользователя.
    public String getUserCards(Long chatId) {
        // Находим пользователя.
        User user = userService.getUser(chatId);
        if (user == null) {
            return "У вас нет добавленных карт!";
        }
        // Получаем список карт пользователя
        List<Card> cards = user.getCards();
        // Если список пуст, то сообщаем об этом.
        if (cards.size() == 0) {
            return "У вас нет добавленных карт!";
            // Иначе отправляем карты пользователя.
        } else {
            StringBuilder text = new StringBuilder("Вы добавили следующие карты:");
            for (int i = 0; i < cards.size(); i++) {
                text.append("\n").append(i + 1).append(") ")
                        .append(cards.get(i).getCardType().getInRussian())
                        .append(" №").append(cards.get(i).getCardNumber());
            }
            return text.toString();
        }
    }

    // Сообщаем баланс карты.
    public String cardBalance(String cardNumber) {
        // Переменная для значения баланса.
        String balance;
        Card card = getCardWithBalance(cardNumber);
        if (card.getCardType() == CardType.BANKING) {
            int debt = card.getBalance();
            if (debt == 0) {
                balance = "Задолженности по банковской карте №" + cardNumber + " не обнаружены, в стоп-листе не находится." +
                        "\n" +
                        "\nДля просмотра истории поездок вы можете воспользоваться личным кабинете пассажира:" +
                        "\nwww.securepayments.sberbank.ru/sberbilet" +
                        "\n" +
                        "\nО том как пользоваться кабинетом можно прочитать здесь:" +
                        "\nwww.krasinform.ru/faq" +
                        "\n" +
                        "\nЕсли у вас не открывается сайт с личным кабинетом пассажира:" +
                        "\nwww.sberbank.ru/ru/certificates";
                // Если задолженность есть.
            } else {
                balance = "Карта №" + cardNumber + " находится в стоп-листе," +
                        " задолженность составляет " + debt + " руб." +
                        "\n" +
                        "\nДля оплаты задолженности, необходимо зарегистрироваться в личном кабинете пассажира:" +
                        "\nwww.securepayments.sberbank.ru/sberbilet" +
                        "\n" +
                        "\nО том как пользоваться кабинетом можно прочитать здесь:" +
                        "\nwww.krasinform.ru/faq" +
                        "\n" +
                        "\nЕсли у вас не открывается сайт с личным кабинетом пассажира:" +
                        "\nwww.sberbank.ru/ru/certificates";
            }
        } else if (card.getCardType() == CardType.TRANSPORT) {
            int debt = card.getBalance();
            balance = "Баланс транспортной карты №" + cardNumber
                    + " = " + debt + " руб."
                    + "\nБаланс онлайн пополнений смотрите на сайте - www.krascard.ru";
        } else {
            int baseTrips = card.getBaseTrips();
            int dopTrips = card.getDopTrips();
            balance = "Баланс социальной карты №" + cardNumber
                    + "\nБазовых поездок = " + baseTrips
                    + "\nДополнительных поездок = " + dopTrips
                    + "\nПериоды действия и детализацию поездок смотрите на сайте - www.krascard.ru";
        }
        return balance;
    }

    // Баланс карты.
    Card getCardWithBalance(String cardNumber) {
        // Создаём карту
        Card card = cardBuilder(cardNumber);
        // Баланс банковской карты.
        if (card.getCardType() == CardType.BANKING) {
            setBankCardBalance(card);
        }
        // Баланс транспортной карты.
        if (card.getCardType() == CardType.TRANSPORT) {
            setTransportCardBalance(card);
        }
        // Баланс социальной карты.
        if (card.getCardType() == CardType.SOCIAL) {
            setSocialCardBalance(card);
        }
        return card;
    }

    // Создание карты - БК, ТК, СК.
    private Card cardBuilder(String cardNumber) {
        // Создаём карту.
        Card card = new Card();
        // Если количество цифр == 16 и первая цифра != 0 (0 у символов char это 48), значит банковская карта.
        if (cardNumber.length() == 16 && cardNumber.charAt(0) != 48) {
            card.setCardType(CardType.BANKING); // задаём тип карты
            // Если количество цифр == 16 значит транспортная.
        } else if (cardNumber.length() == 16) {
            card.setCardType(CardType.TRANSPORT); // задаём тип карты
            // Если количество цифр == 12 социальная карта.
        } else if (cardNumber.length() == 12) {
            card.setCardType(CardType.SOCIAL); // задаём тип карты
        } else {
            // Если нечего не подошло, значит неизвестный тип карты.
            throw new ValidatorExceptions("Неизвестный тип карты, проверьте номер!");
        }
        // Если тип карты определился, устанавливаем номер карты.
        card.setCardNumber(cardNumber);
        return card;
    }

    private void setBankCardBalance(Card card) {
        // Создаём клиент для отправки запросов, таймаут подключения = 10 сек, так как сбербанк задумчивый.
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10000, TimeUnit.MILLISECONDS)
                .build();
        // Выполняем POST запрос на сайт сбербанка.
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create("{\"key\":\"" + card.getCardNumber() + "\"}", mediaType);
        Request request = new Request.Builder()
                .url("https://securepayments.sberbank.ru/server/api/v1/acl/debt")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();
        // Выполняем запрос.
        try (Response response = client.newCall(request).execute()) {
            // Проверяем был ли запрос успешен.
            if (!response.isSuccessful()) {
                throw new IOException("Запрос к серверу не был успешен: " +
                        response.code() + " " + response.message());
            }
            // Тело ответа.
            HashMap<String, String> responseBody = new ObjectMapper().readValue(response.body().string(), HashMap.class);
            // Устанавливаем баланс банковской карты.
            card.setBalance(Integer.parseInt(responseBody.entrySet().iterator().next().getValue()) / 100);
        } catch (IOException e) {
            System.out.println("Ошибка подключения: " + e);
        }
    }

    private void setTransportCardBalance(Card card) {
        // Создаём клиент для отправки запросов.
        OkHttpClient client = new OkHttpClient.Builder()
                .build();
        // Параметры GET запрос на сайт минтранс24.
        Request request = new Request.Builder()
                .url("http://mintrans24.ru/api/services/card/balance/" + card.getCardNumber())
                .method("GET", null)
                .build();
        // Выполняем запрос.
        try (Response response = client.newCall(request).execute()) {
            // Проверяем был ли запрос успешен.
            if (!response.isSuccessful()) {
                throw new IOException("Запрос к серверу не был успешен: " +
                        response.code() + " " + response.message());
            }
            // Тело ответа.
            HashMap<String, String> responseBody = new ObjectMapper().readValue(response.body().string(), HashMap.class);
            // Если ошибка в номере карты.
            if (responseBody.get("error") != null) {
                throw new ValidatorExceptions("Неизвестный тип карты, проверьте номер!");
            }
            // Устанавливаем баланс транспортной карты.
            card.setBalance(Integer.valueOf(responseBody.get("balance").split("\\.")[0]));
        } catch (IOException e) {
            System.out.println("Ошибка подключения: " + e);
        }
    }

    private void setSocialCardBalance(Card card) {
        // Создаём клиент для отправки запросов.
        OkHttpClient client = new OkHttpClient.Builder()
                .build();
        // Параметры GET запрос на сайт минтранс24.
        Request request = new Request.Builder()
                .url("http://mintrans24.ru/api/services/card/balance/" + card.getCardNumber())
                .method("GET", null)
                .build();
        // Выполняем запрос.
        try (Response response = client.newCall(request).execute()) {
            // Проверяем был ли запрос успешен.
            if (!response.isSuccessful()) {
                throw new IOException("Запрос к серверу не был успешен: " +
                        response.code() + " " + response.message());
            }
            // Тело ответа.
            HashMap<String, String> responseBody = new ObjectMapper().readValue(response.body().string(), HashMap.class);
            // Если ошибка в номере карты.
            if (responseBody.get("error") != null) {
                throw new ValidatorExceptions("Неизвестный тип карты, проверьте номер!");
            }
            // Устанавливаем баланс базовых и дополнительных поездок социальной карты.
            card.setBaseTrips(Integer.valueOf(responseBody.get("left_base_trips")));
            card.setDopTrips(Integer.valueOf(responseBody.get("left_dop_trips")));
        } catch (IOException e) {
            System.out.println("Ошибка подключения: " + e);
        }
    }
}