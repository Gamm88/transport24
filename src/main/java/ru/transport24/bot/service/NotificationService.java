package ru.transport24.bot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.transport24.bot.exception.ValidatorExceptions;
import ru.transport24.bot.model.Card;
import ru.transport24.bot.model.CardType;
import ru.transport24.bot.model.MessageType;
import ru.transport24.bot.model.User;
import ru.transport24.bot.repository.UserRepository;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    final TelegramBot telegramBot;
    final NewsService newsService;
    final CardService cardService;
    final UserRepository userRepository;

    // Метод запускается каждый каз после запуска приложения, стартер в классе - BotInitializer.
    // Таймер запуска ежедневно в 22:00 и 10:00.
    public void Timer() {
        // Задание для таймера, запуск методов - startNotification и parseNews.
        TimerTask task = new TimerTask() {
            public void run() {
                try {
                    startNotification();
                    newsService.parseNews();
                } catch (ValidatorExceptions | TelegramApiException | IOException e) {
                    log.info("Ошибка в работе Timer! " + e);
                    telegramBot.sendMessage(1709421744L, "Ошибка в работе Timer! " + e, MessageType.OTHER);
                }
            }
        };

        // Календарь - текущая дата в 22:00.
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 22);
        today.set(Calendar.MINUTE, 0);
        // Создаём таймер - задание (метод task), время начала (today), повторного срабатывания (через 12 часов)
        Timer timer = new Timer("Timer");
        timer.schedule(task, today.getTime(), 43200000);
        log.info("Timer запущен!");
        telegramBot.sendMessage(1709421744L, "Timer запущен!", MessageType.OTHER);
    }

    // Метод отправки уведомлений пользователям если их карта в стоп-листе, мало средств, поездок.
    private void startNotification() throws IOException, TelegramApiException {
        // Список пользователей у кого включены уведомления.
        List<User> userList = userRepository.findAllByNotificationIsTrue();
        // Для каждого пользователя из списка.
        for (User user : userList) {
            // Получаем ИД пользователя.
            Long userId = user.getId();
            // Получаем список карт пользователя.
            List<Card> cardList = user.getCards();
            // Проверяем наличие карт в списке.
            if (cardList.size() > 0) {
                // Для каждой карты выполняем проверку баланса.
                for (Card card : cardList) {
                    // Получаем номер карты.
                    String cardNumber = card.getCardNumber();
                    // Если карта банковская.
                    if (card.getCardType() == CardType.BANKING) {
                        int balance = 0;
                        try {
                            balance = cardService.getCardWithBalance(cardNumber).getBalance();
                        } catch (ValidatorExceptions e) {
                            log.info("Ошибка проверки баланса карты: " + e);
                            telegramBot.sendMessage(1709421744L, "Ошибка проверки баланса карты: " + e, MessageType.OTHER);
                        }
                        if (balance > 0) {
                            telegramBot.sendMessage(userId, "Банковская карта №" + cardNumber + " попала в стоп-лист," +
                                    " задолженность составляет " + balance + " руб.", MessageType.OTHER);
                        }
                    }
                    // Если карта транспортная.
                    if (card.getCardType() == CardType.TRANSPORT) {
                        int balance = 100;
                        try {
                            balance = cardService.getCardWithBalance(cardNumber).getBalance();
                        } catch (ValidatorExceptions e) {
                            log.info("Ошибка проверки баланса карты: " + e);
                            telegramBot.sendMessage(1709421744L, "Ошибка проверки баланса карты: " + e, MessageType.OTHER);
                            cardService.deleteCard(userId, cardNumber);
                        }
                        if (balance < 100) {
                            telegramBot.sendMessage(userId, "На транспортной карте №" + cardNumber +
                                    " осталось " + balance + " руб.", MessageType.OTHER);
                        }
                    }
                    // Если карта социальная.
                    if (card.getCardType() == CardType.SOCIAL) {
                        int balance = 3;
                        try {
                            balance = cardService.getCardWithBalance(cardNumber).getBaseTrips()
                                    + cardService.getCardWithBalance(cardNumber).getDopTrips();
                        } catch (ValidatorExceptions e) {
                            log.info("Ошибка проверки баланса карты: " + e);
                            telegramBot.sendMessage(1709421744L, "Ошибка проверки баланса карты: " + e, MessageType.OTHER);
                        }
                        if (balance < 3) {
                            telegramBot.sendMessage(userId, "На социальной карте №" + cardNumber +
                                    " осталось " + balance + " поездок.", MessageType.OTHER);
                        }
                    }
                }
            }
        }
    }
}