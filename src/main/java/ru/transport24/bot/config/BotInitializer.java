package ru.transport24.bot.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.transport24.bot.service.NotificationService;
import ru.transport24.bot.service.TelegramBot;

@Slf4j
@Component
@RequiredArgsConstructor
public class BotInitializer {
    final TelegramBot telegramBot;
    final NotificationService notificationService;

    // Регистрация и запуск бота
    @EventListener({ContextRefreshedEvent.class})
    public void init() throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        try {
            telegramBotsApi.registerBot(telegramBot);
        } catch (TelegramApiException e) {
            log.error("Ошибка Telegram Api: " + e.getMessage());
        }
    }

    // Запуск методов, после запуска приложения.
    @EventListener(ApplicationReadyEvent.class)
    public void runAfterStartup() {
        notificationService.Timer(); // метод отправки уведомлений по расписанию (ежедневно в 22:00 и 10:00)
    }
}
