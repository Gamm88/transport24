package ru.transport24.bot.service;

import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.webapp.WebAppInfo;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.transport24.bot.config.BotConfig;
import ru.transport24.bot.exception.ValidatorExceptions;
import ru.transport24.bot.model.News;
import ru.transport24.bot.model.MessageType;
import ru.transport24.bot.model.User;

import java.util.*;

@Slf4j
@Service
public class TelegramBot extends TelegramLongPollingBot {
    final Long adminId;
    final BotConfig botConfig;
    final UserService userService;
    final CardService cardService;
    final NewsService newsService;
    final MarkupService markupService;
    final MessageService messageService;

    public TelegramBot(BotConfig botConfig, NewsService newsService, UserService userService,
                       MessageService messageService, MarkupService markupService, CardService cardService) {
        super(botConfig.getBotToken());
        this.botConfig = botConfig;
        this.userService = userService;
        this.cardService = cardService;
        this.newsService = newsService;
        this.markupService = markupService;
        this.messageService = messageService;

        // Создаём меню бота
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/schedule", "Маршруты, расписание, билеты"));
        listOfCommands.add(new BotCommand("/online", "Онлайн движение транспорта"));
        listOfCommands.add(new BotCommand("/track", "Контроль карт оплаты проезда"));
        listOfCommands.add(new BotCommand("/bcard", "Оплата проезда Банковской картой"));
        listOfCommands.add(new BotCommand("/tcard", "Оплата проезда Транспортной картой"));
        listOfCommands.add(new BotCommand("/scard", "Оплата проезда Социальной картой"));
        listOfCommands.add(new BotCommand("/qrcod", "Оплата проезда по QR-коду"));
        listOfCommands.add(new BotCommand("/news", "Новости"));
        listOfCommands.add(new BotCommand("/feedback", "Обратная связь"));
        listOfCommands.add(new BotCommand("/help", "О проекте"));
        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Ошибка " + e.getMessage());
            throw new RuntimeException(e);
        }
        adminId = botConfig.getBotAdmin();
    }

    // имя бота, используется для запуска
    @Override
    public String getBotUsername() {
        return botConfig.getBotUsername();
    }

    // Метод вызывается всякий раз, когда будет доступно новое обновление (входящее сообщение у бота).
    @Override
    public void onUpdateReceived(Update update) {
        // Обработка сообщений типа - CallbackQuery (кнопка).
        if (update.hasCallbackQuery()) {
            // Определяем кнопку.
            String data = update.getCallbackQuery().getData();
            log.info("Нажата кнопка => " + data);
            // ИД чата (пользователя), где пришло сообщение.
            Long chatId = update.getCallbackQuery().getMessage().getChat().getId();
            // Действия.
            // Карты добавленные пользователем.
            if ("USER_CARDS".equals(data)) {
                sendMessage(chatId, cardService.getUserCards(chatId), MessageType.OTHER);
                return;
            }
            // Ответное сообщение
            sendMessage(chatId, data, MessageType.BUTTON);
            return;
        }

        // Входящее сообщение.
        Message incomingMessage = update.getMessage();
        // ИД чата (пользователя), где пришло сообщение.
        Long chatId = incomingMessage.getChat().getId();

        // Текст сообщения.
        String text;
        if (incomingMessage.getText() != null) {
            text = incomingMessage.getText().toLowerCase();    // если просто текст
        } else {
            text = incomingMessage.getCaption().toLowerCase(); // если текст с фото
        }
        log.info("Новое сообщение => " + text);

        // При старте бота, регистрируем пользователя и сообщаем админу.
        if (text.equals("/start")) {
            userService.addUser(update);
            User user = userService.getUser(chatId);
            if (user != null) {
                String newUser = "Новый пользователь => ID - " + user.getId() +
                        ", UserName - " + user.getUserName() +
                        ", FirstName - " + user.getFirstName() +
                        ", LastName - " + user.getLastName();
                sendMessage(adminId, newUser, MessageType.OTHER);
            } else {
                sendMessage(adminId, "Ошибка добавления пользователя с ID " + chatId, MessageType.OTHER);
            }
        }

        // Обработка команд администратора (ИД чата = ИД администратора).
        if (chatId.equals(adminId)) {
            // Отправка сообщения пользователю по форме - sm [ид пользователя] {сообщение}
            if (text.toLowerCase().contains("sm")) {
                Long userID = Long.valueOf(text.substring(text.indexOf("[") + 1, text.indexOf("]")));
                String massage = text.substring(text.indexOf("{") + 1, text.indexOf("}"));
                sendMessage(userID, massage, MessageType.OTHER);
            }
        }

        // Обработка команд.
        if (incomingMessage.isCommand()) {
            if (text.equals("/news")) {
                for (long i = 1; i < 6; i++) {
                    News news = newsService.getNews(6 - i);
                    sendNews(chatId, news);
                }
            }
            sendMessage(chatId, text, MessageType.COMMAND);
            return;
        }

        // Добавление карты для контроля (текст содержит ключевое слово - добавить).
        if (text.toLowerCase().contains("добавить")) {
            sendMessage(chatId, cardService.addCard(chatId, text, update), MessageType.OTHER);
            return;
        }

        // Отмена контроля карты (текст содержит ключевое слово - удалить).
        if (text.toLowerCase().contains("удалить")) {
            sendMessage(chatId, cardService.deleteCard(chatId, text), MessageType.OTHER);
            return;
        }

        // Ошибки, предложения и жалобы отправленные в чат.
        if (text.toLowerCase().contains("ошибк") || text.toLowerCase().contains("предлож") || text.toLowerCase().contains("жалоб")) {
            sendMessage(adminId, userService.getUser(chatId).toString() + "\n" + text, MessageType.OTHER);
            return;
        }

        // Возможно пользователь хочет узнать баланс карты - Банковской, Транспортной, Социальной.
        if (text.length() > 11) {
            // Получаем номер карты из сообщения (удаляем все кроме цифр).
            String cardNumber = update.getMessage().getText().replaceAll("\\D+", "");
            // Если количество цифр > 11, то выполняем проверку баланса карты или нахождение в стоп-листе.
            if (cardNumber.length() > 11) {
                try {
                    sendMessage(chatId, cardService.cardBalance(cardNumber), MessageType.OTHER);
                } catch (ValidatorExceptions e) {
                    sendMessage(chatId, e.getMessage(), MessageType.OTHER);
                }
            }
            return;
        }

        // Подбор ответов на текст в чате.
        if (text.contains("лист") || text.contains("стоп")) {
            sendMessage(chatId, "BANK_CARD_STOP_LIST", MessageType.BUTTON);
        }
        if (text.contains("баланс")) {
            sendMessage(chatId, "BALANCE", MessageType.BUTTON);
        }
        if (text.contains("сбербилет")) {
            sendMessage(chatId, "BANK_CARD_SBER_BILET", MessageType.BUTTON);
        }

        // Если нечего выше не сработало, отправляем сообщение админу - ИД пользователя и текст сообщения.
        sendMessage(adminId, chatId + " написал - " + text, MessageType.OTHER);
    }

    // Отправка сообщения.
    void sendMessage(Long chatId, String text, MessageType messageType) {
        // Создаём отправляемое сообщение.
        SendMessage sendMessage = SendMessage.builder()
                .disableWebPagePreview(true)            // Отключаем веб представление сайтов.
                .chatId(chatId.toString())              // кому отправляем сообщение (ИД чата).
                .text(EmojiParser.parseToUnicode(text)) // текст сообщения (с поддержкой смайлов).
                .build();
        // Если тип обновление не прочее, текст сообщение и клавиатура устанавливаются из соответствующего сервиса.
        if (!messageType.equals(MessageType.OTHER)) {
            sendMessage.setText(EmojiParser.parseToUnicode(messageService.getMessage(text)));
            sendMessage.setReplyMarkup(markupService.getMarkup(text));
        }
        // Отправляем сообщение.
        try {
            execute(sendMessage);
            log.info("\nОтправлено сообщение => " + sendMessage.getText());
        } catch (TelegramApiException e) {
            // Возможно бот заблокирован пользователем, удаляем пользователя из БД.
            if (e.getMessage().contains("bot was blocked by the user")) {
                userService.deleteUser(chatId);
            } else {
                sendMessage(adminId, e.getMessage(), MessageType.OTHER);
            }
        }
    }

    //  Отправка новости.
    void sendNews(Long chatId, News news) {
        // Клавиатура
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        // Ряды клавиатуры
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        // 1 ряд клавиатуры
        List<InlineKeyboardButton> rowInLine1 = new ArrayList<>();
        // Кнопка Web - Читать полностью (страница новости).
        var newsLink = new InlineKeyboardButton();
        var newsWeb = new WebAppInfo(news.getLink());
        System.out.println(newsWeb);
        newsLink.setText("Читать полностью");
        newsLink.setWebApp(newsWeb);

        rowInLine1.add(newsLink);
        rowsInLine.add(rowInLine1);
        markup.setKeyboard(rowsInLine);


        // Отправляем новость - дата публикации + заголовок + кнопка (ссылка на полное содержании новости).
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId.toString())
                .text(news.getDate() + "\n" + news.getTitle() + "\n" + news.getLink())
                //.replyMarkup(markup)
                .build();

        try {
            execute(sendMessage);
            log.info("\nОтправлена новость => " + sendMessage.getText());
        } catch (TelegramApiException e) {
            log.info("\nОшибка отправки новость => " + e);
        }
    }
}