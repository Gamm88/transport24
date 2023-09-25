package ru.transport24.bot.service;

import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
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
    final ChatGPT chatGPT;
    final BotConfig botConfig;
    final UserService userService;
    final CardService cardService;
    final NewsService newsService;
    final MarkupService markupService;
    final MessageService messageService;


    public TelegramBot(BotConfig botConfig, NewsService newsService, UserService userService,
                       MessageService messageService, MarkupService markupService, CardService cardService, ChatGPT chatGPT) {
        super(botConfig.getBotToken());
        this.chatGPT = chatGPT;
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
        listOfCommands.add(new BotCommand("/taxi", "Деятельность легкового такси"));
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
            log.error("\nОшибка создании меню " + e.getMessage());
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
        log.info("\nНовое обновление => " + update);
        // Обработка сообщений типа - CallbackQuery (кнопка).
        if (update.hasCallbackQuery()) {
            // Определяем кнопку.
            String data = update.getCallbackQuery().getData();
            log.info("\nНажата кнопка => " + data);
            // Определяем ИД сообщения.
            Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
            // ИД чата (пользователя), где пришло сообщение.
            Long chatId = update.getCallbackQuery().getMessage().getChat().getId();
            // Действия.
            // Карты добавленные пользователем.
            if (data.equals("USER_CARDS")) {
                sendMessage(chatId, cardService.getUserCards(chatId), MessageType.OTHER);
                return;
            }
            if (data.equals("TRACK_ADD_CARD")) {
                sendMessage(chatId, data, MessageType.BUTTON);
                return;
            }
            // Ответное сообщение
            editMessage(messageId, chatId, data);
            return;
        }

        // Обрабатываем входящее сообщение.
        Message incomingMessage;
        Long chatId;
        // Пытаемся получить сообщение.
        try {
            // Содержание сообщения.
            incomingMessage = update.getMessage();
            // ИД чата (пользователя), где пришло сообщение.
            chatId = incomingMessage.getChat().getId();
        } catch (NullPointerException e) {
            // Если нет сообщения возможно блокировка или разблокировка бота.
            if (update.getMyChatMember() != null && update.getMyChatMember().getNewChatMember().getStatus().equals("kicked")) {
                log.info("\nПользователь заблокировал бота");
                return;
            } else if (update.getMyChatMember() != null && update.getMyChatMember().getNewChatMember().getStatus().equals("member")) {
                log.info("\nПользователь разблокировал бота");
                return;
                // Иначе неизвестное обновление.
            } else {
                log.info("\nНеизвестное обновление - " + update);
                sendMessage(adminId, "Неизвестное обновление - " + update, MessageType.OTHER);
                return;
            }
        }

        // Текст сообщения.
        String text;
        if (incomingMessage.getText() != null) {
            text = incomingMessage.getText().toLowerCase();    // если просто текст
        } else {
            text = incomingMessage.getCaption().toLowerCase(); // если текст с фото
        }
        log.info("\nПришло сообщение => " + text);

        // Обработка команд администратора (ИД чата = ИД администратора).
        if (chatId.equals(adminId)) {
            // Отправка сообщения пользователю по форме - sm [ид пользователя] {сообщение}
            if (text.toLowerCase().contains("sm")) {
                text = incomingMessage.getText();
                Long userID = Long.valueOf(text.substring(text.indexOf("[") + 1, text.indexOf("]")));
                String massage = text.substring(text.indexOf("{") + 1, text.indexOf("}"));
                sendMessage(userID, massage, MessageType.OTHER);
                return;
            }
        }

        // Обработка команд.
        if (incomingMessage.isCommand()) {
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
                // Отправка новостей.
            } else if (text.equals("/news")) {
                for (long i = 1; i < 6; i++) {
                    News news = newsService.getNews(6 - i);
                    sendNews(chatId, news);
                }
            }
            sendMessage(chatId, text, MessageType.COMMAND);
            return;
        }

        // Добавление карты для контроля (текст содержит ключевое слово - добавить).
        if (text.toLowerCase().contains("добавит")) {
            // Если в сообщении нет цифры отправляет сообщение с тем как добавить карту.
            if (text.replaceAll("\\D+", "").length() < 3) {
                sendMessage(chatId, "TRACK_ADD_CARD", MessageType.BUTTON);
                return;
            }
            // Если есть цифры выполняем метод добавления карты.
            try {
                sendMessage(chatId, cardService.addCard(chatId, text, update), MessageType.OTHER);
            } catch (ValidatorExceptions e) {
                sendMessage(chatId, e.getMessage(), MessageType.OTHER);
            }
            return;
        }

        // Отмена контроля карты (текст содержит ключевое слово - удалить).
        if (text.toLowerCase().contains("удалит")) {
            // Если в сообщении нет цифры отправляет сообщение с тем как добавить карту.
            if (text.replaceAll("\\D+", "").length() < 3) {
                sendMessage(chatId, "TRACK_DELETE_CARD", MessageType.BUTTON);
                return;
            }
            sendMessage(chatId, cardService.deleteCard(chatId, text), MessageType.OTHER);
            return;
        }

        // Ошибки, предложения и жалобы отправленные в чат.
        if (text.toLowerCase().contains("ошибк") || text.toLowerCase().contains("предлож")
                || text.toLowerCase().contains("жалоб") || text.toLowerCase().contains("вопрос")) {
            sendMessage(adminId, userService.getUser(chatId).toString() + "\n" + text, MessageType.OTHER);
            return;
        }

        // Возможно пользователь хочет узнать баланс карты - Банковской, Транспортной, Социальной.
        // Получаем номер карты из сообщения (удаляем все кроме цифр).
        String cardNumber = update.getMessage().getText().replaceAll("\\D+", "");
        // Если цифры есть и их больше 3, то выполняем проверку баланса карты или нахождение в стоп-листе.
        if (cardNumber.length() > 3) {
            try {
                sendMessage(chatId, cardService.cardBalance(cardNumber), MessageType.OTHER);
            } catch (ValidatorExceptions e) {
                sendMessage(chatId, e.getMessage(), MessageType.OTHER);
            }
            return;
        }

        // Подбор ответов на текст в чате.
        if (text.contains("отзыв")) {
            sendMessage(chatId, "/feedback", MessageType.COMMAND);
            return;
        } else if (text.contains("вывес") || text.contains("снять")) {
            sendMessage(chatId, "BANK_CARD_STOP_LIST_REMOVE", MessageType.BUTTON);
            return;
        } else if (text.contains("стоп") || text.contains("черн") || text.contains("чёрн") || text.contains("блокиров") || text.contains("не могу оплатить")) {
            sendMessage(chatId, "BANK_CARD_STOP_LIST", MessageType.BUTTON);
            return;
        } else if (text.contains("баланс")) {
            sendMessage(chatId, "BALANCE", MessageType.BUTTON);
            return;
        } else if (text.contains("сбербилет")) {
            sendMessage(chatId, "BANK_CARD_SBER_BILET", MessageType.BUTTON);
            return;
        } else if (text.contains("меню")) {
            sendMessage(chatId, "/start", MessageType.COMMAND);
            return;
        } else if (text.contains("такси")) {
            sendMessage(chatId, "/taxi", MessageType.COMMAND);
            return;
        } else if (text.contains("разрешен") || text.contains("госпошлин")) {
            sendMessage(chatId, "WHO_CAN_WORK_IN_TAXI", MessageType.BUTTON);
            return;
        } else if (text.contains("вернуть")) {
            sendMessage(chatId, "BANK_CARD_REFUND", MessageType.BUTTON);
            return;
        } else if (text.contains("сайт") || text.contains("ссылка") && text.contains("не") && text.contains("работ") || text.contains("открыв")) {
            sendMessage(chatId, "/news", MessageType.COMMAND);
            return;
        } else if (text.contains("расписание") || text.contains("какая маршрутка") || text.contains("какой автобус")) {
            sendMessage(chatId, "/schedule", MessageType.COMMAND);
            return;
        }

        // Если нечего выше не сработало, ответит искусственный интеллект.
        String answerChatGPT = chatGPT.askChatGPT(text);
        sendMessage(chatId, answerChatGPT, MessageType.OTHER);
        // Отправляем сообщение админу.
        sendMessage(adminId, chatId + " написал:\n" + text + "\nОтвет GPT:\n" + answerChatGPT, MessageType.OTHER);
    }

    // Отправка сообщения.
    void sendMessage(Long chatId, String text, MessageType messageType) {
        // Создаём отправляемое сообщение.
        SendMessage sendMessage = SendMessage.builder()
                .disableWebPagePreview(true)            // Отключаем веб представление сайтов.
                .parseMode(ParseMode.HTML)              // Ссылки по тексту - href.
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

    // Редактируем ранее отправленное сообщение.
    void editMessage(Integer messageId, Long chatId, String data) {
        // Редактируем сообщение.
        EditMessageText sendMessage = EditMessageText.builder()
                .messageId(messageId)
                .disableWebPagePreview(true)
                .parseMode(ParseMode.HTML)
                .chatId(chatId.toString())
                .text(EmojiParser.parseToUnicode(messageService.getMessage(data)))
                .replyMarkup(markupService.getMarkup(data))
                .build();
        // Изменяем сообщение.
        try {
            execute(sendMessage);
            log.info("\nИзменено сообщение => " + sendMessage.getText());
        } catch (TelegramApiException e) {
            log.info("\nОшибка изменения сообщения => " + e.getMessage() +
                    "\nВходные данные: => " + data +
                    "\nСообщение => " + sendMessage);
        }
    }

    //  Отправка новости.
    void sendNews(Long chatId, News news) {
        // Отправляем новость - дата публикации + заголовок + кнопка (ссылка на полное содержании новости).
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId.toString())
                .text(news.getDate() + "\n" + news.getTitle() + "\n" + news.getLink())
                .build();
        try {
            execute(sendMessage);
            log.info("\nОтправлена новость => " + sendMessage.getText());
        } catch (TelegramApiException e) {
            log.info("\nОшибка отправки новость => " + e);
        }
    }
}