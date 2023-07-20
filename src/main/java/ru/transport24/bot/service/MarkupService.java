package ru.transport24.bot.service;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.webapp.WebAppInfo;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarkupService {
    final NewsService newsService;

    // Заготовка для разных клавиатур
    public InlineKeyboardMarkup getMarkup(String data) {
        // Клавиатура
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        // Ряды клавиатуры
        List<List<InlineKeyboardButton>> rowsInLine = new ArrayList<>();
        // 1 ряд клавиатуры
        List<InlineKeyboardButton> rowInLine1 = new ArrayList<>();
        // 2 ряд клавиатуры
        List<InlineKeyboardButton> rowInLine2 = new ArrayList<>();
        // 3  ряд клавиатуры
        List<InlineKeyboardButton> rowInLine3 = new ArrayList<>();
        // 4  ряд клавиатуры
        List<InlineKeyboardButton> rowInLine4 = new ArrayList<>();
        // 5  ряд клавиатуры
        List<InlineKeyboardButton> rowInLine5 = new ArrayList<>();

        /*
        Раздел - Оплата проезда Банковской картой
        */

        // Кнопка - Банковская карта - Почему карта попадает в стоп-лист
        var bankCardStopList = new InlineKeyboardButton();
        bankCardStopList.setText("Почему карта попадает в стоп-лист");
        bankCardStopList.setCallbackData("BANK_CARD_STOP_LIST");

        // Кнопка - Банковская карта - Вывести карту из стоп-листа
        var bankCardStopListRemove = new InlineKeyboardButton();
        bankCardStopListRemove.setText("Вывести карту из стоп-листа");
        bankCardStopListRemove.setCallbackData("BANK_CARD_STOP_LIST_REMOVE");

        // Кнопка - Банковская карта - Личный кабинет пассажира СберБилет
        var bankCardSberBilet = new InlineKeyboardButton();
        bankCardSberBilet.setText("Личный кабинет пассажира");
        bankCardSberBilet.setCallbackData("BANK_CARD_SBER_BILET");

        // Кнопка - банковская карта - возврат средств
        var bankCardRefund = new InlineKeyboardButton();
        bankCardRefund.setText("Возврат за незавершённую поездку");
        bankCardRefund.setCallbackData("BANK_CARD_REFUND");

        // Кнопка Web - банковская карта - возврат средств для стационарного терминала (валидатора)
        var bankCardRefundValidatorLink = new InlineKeyboardButton();
        var bankCardRefundValidatorWeb = new WebAppInfo("https://aikom.ru/vozvrat/");
        bankCardRefundValidatorLink.setText("1) Стационарный терминал");
        bankCardRefundValidatorLink.setWebApp(bankCardRefundValidatorWeb);

        // Кнопка Web - банковская карта - возврат средств для переносного терминала
        var bankCardRefundTerminalLink = new InlineKeyboardButton();
        var bankCardRefundTerminalWeb = new WebAppInfo("https://forms.yandex.ru/u/6422abd0d0468809ad56e59d/");
        bankCardRefundTerminalLink.setText("2) Переносной терминал");
        bankCardRefundTerminalLink.setWebApp(bankCardRefundTerminalWeb);

        // Кнопка - банковская карта - проверить стоп-лист
        var bankCardCheckStopList = new InlineKeyboardButton();
        bankCardCheckStopList.setText("Проверить стоп-лист");
        bankCardCheckStopList.setCallbackData("BANK_CARD_CHECK_STOP_LIST");

        // Кнопка - Банковская карта - Назад
        var bankCardBack = new InlineKeyboardButton();
        bankCardBack.setText("<- Назад");
        bankCardBack.setCallbackData("BANK_CARD_BACK");

        /*
        Раздел - Оплата проезда Транспортной картой
        */

        // Кнопка - транспортная карта - общая информация
        var transportCardInfo = new InlineKeyboardButton();
        transportCardInfo.setText("Общая информация");
        transportCardInfo.setCallbackData("TRANSPORT_CARD_INFO");

        // Кнопка - транспортная карта - способы пополнения
        var transportCardReplenishment = new InlineKeyboardButton();
        transportCardReplenishment.setText("Способы пополнения");
        transportCardReplenishment.setCallbackData("TRANSPORT_CARD_REPLENISHMENT");

        // Кнопка - транспортная карта - безлимитный тариф
        var transportCardUnlimited = new InlineKeyboardButton();
        transportCardUnlimited.setText("Безлимитный проездной");
        transportCardUnlimited.setCallbackData("TRANSPORT_CARD_UNLIMITED");

        // Кнопка - транспортная карта - техническая поддержка
        var transportCardSupport = new InlineKeyboardButton();
        transportCardSupport.setText("Техническая поддержка");
        transportCardSupport.setCallbackData("TRANSPORT_CARD_SUPPORT");

        // Кнопка - транспортная карта - узнать баланс
        var transportCardBalance = new InlineKeyboardButton();
        transportCardBalance.setText("Узнать баланс");
        transportCardBalance.setCallbackData("TRANSPORT_CARD_BALANCE");

        // Кнопка - социальная карта - общая информация
        var socialCardInfo = new InlineKeyboardButton();
        socialCardInfo.setText("Общая информация");
        socialCardInfo.setCallbackData("SOCIAL_CARD_INFO");

        // Кнопка - социальная карта - способы пополнения
        var socialCardReplenishment = new InlineKeyboardButton();
        socialCardReplenishment.setText("Порядок пополнения");
        socialCardReplenishment.setCallbackData("SOCIAL_CARD_REPLENISHMENT");

        // Кнопка - социальная карта - техническая поддержка
        var socialCardSupport = new InlineKeyboardButton();
        socialCardSupport.setText("Техническая поддержка");
        socialCardSupport.setCallbackData("SOCIAL_CARD_SUPPORT");

        // Кнопка - социальная карта - узнать баланс
        var socialCardBalance = new InlineKeyboardButton();
        socialCardBalance.setText("Узнать баланс");
        socialCardBalance.setCallbackData("SOCIAL_CARD_BALANCE");

        // Кнопка - контроля баланса БК, ТК и СК - Подключить уведомления
        var trackStart = new InlineKeyboardButton();
        trackStart.setText("Подключить уведомления");
        trackStart.setCallbackData("TRACK_START");

        // Кнопка - контроля баланса БК, ТК и СК - добавить карту
        var trackAddCard = new InlineKeyboardButton();
        trackAddCard.setText("Добавить карту");
        trackAddCard.setCallbackData("TRACK_ADD_CARD");

        // Кнопка - контроля баланса БК, ТК и СК - удалить карту
        var trackDeleteCard = new InlineKeyboardButton();
        trackDeleteCard.setText("Удалить карту");
        trackDeleteCard.setCallbackData("TRACK_DELETE_CARD");

        // Кнопка - контроля баланса БК, ТК и СК - список карт пользователя
        var userCards = new InlineKeyboardButton();
        userCards.setText("Мои карты");
        userCards.setCallbackData("USER_CARDS");

        // Кнопка Web - Найти и купить билет - krasavtovokzal.ru
        var buyTicketsLink = new InlineKeyboardButton();
        var buyTicketsWeb = new WebAppInfo("https://krasavtovokzal.ru/");
        buyTicketsLink.setText("Найти и купить билет");
        buyTicketsLink.setWebApp(buyTicketsWeb);

        // Кнопка Web - Онлайн движение автобусов - Yandex
        var onlineBusYandexLink = new InlineKeyboardButton();
        var onlineBusYandexWeb = new WebAppInfo("https://yandex.ru/maps/62/krasnoyarsk/transport/");
        onlineBusYandexLink.setText("Сервис от Яндекс");
        onlineBusYandexLink.setWebApp(onlineBusYandexWeb);

        // Кнопка Web - Онлайн движение автобусов - 2gis
        var gisLink = new InlineKeyboardButton();
        var gisWeb = new WebAppInfo("https://2gis.ru/krasnoyarsk?layer=eta");
        gisLink.setText("Сервис от 2ГИС");
        gisLink.setWebApp(gisWeb);

        // Кнопка Web - Онлайн движение автобусов - Busti
        var onlineBusBustiLink = new InlineKeyboardButton();
        var onlineBusBustiWeb = new WebAppInfo("https://ru.busti.me/krasnoyarsk/");
        onlineBusBustiLink.setText("Сервис от Busti");
        onlineBusBustiLink.setWebApp(onlineBusBustiWeb);

        // Кнопка Web - Онлайн движение автобусов - Kgt
        var onlineBusKgtLink = new InlineKeyboardButton();
        var onlineBusKgtWeb = new WebAppInfo("https://mu-kgt.ru/informing/wap/marsh/");
        onlineBusKgtLink.setText("Сервис от Красноярскгортранс");
        onlineBusKgtLink.setWebApp(onlineBusKgtWeb);

        // Кнопка Web - Оплата проезда по QR-коду
        var qrCodeLink = new InlineKeyboardButton();
        var qrCodeWeb = new WebAppInfo("https://tr24.krasinform.ru/");
        qrCodeLink.setText("Перейти к приложению");
        qrCodeLink.setWebApp(qrCodeWeb);

        // Подготавливаем клавиатуру для меню или нажатой кнопки.
        switch (data) {
            /*
            МАРШРУТЫ, РАСПИСАНИЯ, БИЛЕТЫ
            */

            // Клавиатура для раздела меню - Маршруты, расписания, билеты.
            case "/schedule" -> {
                rowInLine1.add(buyTicketsLink);

                rowsInLine.add(rowInLine1);

                markup.setKeyboard(rowsInLine);
            }

            /*
            ОНЛАЙН ДВИЖЕНИЕ ТРАНСПОРТА
            */

            // Клавиатура для раздела меню - Онлайн движение транспорта
            case "/online" -> {
                rowInLine1.add(onlineBusYandexLink);
                rowInLine2.add(gisLink);
                rowInLine3.add(onlineBusBustiLink);
                rowInLine4.add(onlineBusKgtLink);

                rowsInLine.add(rowInLine1);
                rowsInLine.add(rowInLine2);
                rowsInLine.add(rowInLine3);
                rowsInLine.add(rowInLine4);

                markup.setKeyboard(rowsInLine);
            }

            /*
            БАНКОВСКАЯ КАРТА
            */

            // Клавиатура для раздела меню - Оплата проезда банковской картой
            case "/bcard", "BANK_CARD_BACK" -> {
                rowInLine1.add(bankCardStopList);
                rowInLine2.add(bankCardStopListRemove);
                rowInLine3.add(bankCardSberBilet);
                rowInLine4.add(bankCardRefund);
                rowInLine5.add(bankCardCheckStopList);

                rowsInLine.add(rowInLine1);
                rowsInLine.add(rowInLine2);
                rowsInLine.add(rowInLine3);
                rowsInLine.add(rowInLine4);
                rowsInLine.add(rowInLine5);

                markup.setKeyboard(rowsInLine);
            }

            // Клавиатура для окна - Почему карта попадает в стоп-лист
            case "BANK_CARD_STOP_LIST" -> {
                rowInLine1.add(bankCardStopListRemove);
                rowInLine2.add(bankCardCheckStopList);
                rowInLine3.add(bankCardBack);

                rowsInLine.add(rowInLine1);
                rowsInLine.add(rowInLine2);
                rowsInLine.add(rowInLine3);

                markup.setKeyboard(rowsInLine);
            }

            // Клавиатура для окна - Вывести карту из стоп-листа.
            case "BANK_CARD_STOP_LIST_REMOVE" -> {
                rowInLine1.add(trackStart);
                rowInLine2.add(bankCardCheckStopList);
                rowInLine3.add(bankCardBack);

                rowsInLine.add(rowInLine1);
                rowsInLine.add(rowInLine2);
                rowsInLine.add(rowInLine3);

                markup.setKeyboard(rowsInLine);
            }

            // Клавиатура для окна - Личный кабинет пассажира СберБилет
            case "BANK_CARD_SBER_BILET" -> {
                rowInLine1.add(trackStart);
                rowInLine2.add(bankCardBack);

                rowsInLine.add(rowInLine1);
                rowsInLine.add(rowInLine2);

                markup.setKeyboard(rowsInLine);
            }

            // Клавиатура для окна - Возврат за незавершённую поездку.
            case "BANK_CARD_REFUND" -> {
                rowInLine1.add(bankCardRefundValidatorLink);
                rowInLine2.add(bankCardRefundTerminalLink);
                rowInLine3.add(bankCardBack);

                rowsInLine.add(rowInLine1);
                rowsInLine.add(rowInLine2);
                rowsInLine.add(rowInLine3);

                markup.setKeyboard(rowsInLine);
            }

            // Клавиатура для окна - Поверить стоп-лист.
            case "BANK_CARD_CHECK_STOP_LIST" -> {
                rowInLine1.add(bankCardBack);

                rowsInLine.add(rowInLine1);

                markup.setKeyboard(rowsInLine);
            }

            /*
            ТРАНСПОРТНАЯ КАРТА
            */

            // Клавиатура для транспортной карты.
            case "/tcard", "TRANSPORT_CARD_INFO", "TRANSPORT_CARD_REPLENISHMENT", "TRANSPORT_CARD_UNLIMITED",
                    "TRANSPORT_CARD_SUPPORT", "TRANSPORT_CARD_BALANCE" -> {
                rowInLine1.add(transportCardInfo);
                rowInLine2.add(transportCardReplenishment);
                rowInLine3.add(transportCardUnlimited);
                rowInLine4.add(transportCardSupport);
                rowInLine5.add(transportCardBalance);

                rowsInLine.add(rowInLine1);
                rowsInLine.add(rowInLine2);
                rowsInLine.add(rowInLine3);
                rowsInLine.add(rowInLine4);
                rowsInLine.add(rowInLine5);

                markup.setKeyboard(rowsInLine);
            }
            // Клавиатура для социальной карты.
            case "/scard", "SOCIAL_CARD_INFO", "SOCIAL_CARD_REPLENISHMENT", "SOCIAL_CARD_SUPPORT",
                    "SOCIAL_CARD_BALANCE" -> {
                rowInLine1.add(socialCardInfo);
                rowInLine2.add(socialCardReplenishment);
                rowInLine3.add(socialCardSupport);
                rowInLine4.add(socialCardBalance);

                rowsInLine.add(rowInLine1);
                rowsInLine.add(rowInLine2);
                rowsInLine.add(rowInLine3);
                rowsInLine.add(rowInLine4);

                markup.setKeyboard(rowsInLine);
            }
            // Клавиатура для контроля баланса БК, ТК и СК
            case "/track", "TRACK_START", "TRACK_ADD_CARD", "TRACK_DELETE_CARD", "USER_CARDS" -> {
                rowInLine1.add(trackAddCard);
                rowInLine1.add(trackDeleteCard);
                rowInLine2.add(userCards);

                rowsInLine.add(rowInLine1);
                rowsInLine.add(rowInLine2);

                markup.setKeyboard(rowsInLine);
            }

            case "/qrcod" -> {
                rowInLine1.add(qrCodeLink);

                rowsInLine.add(rowInLine1);

                markup.setKeyboard(rowsInLine);
            }

            // Если команда не найдена возвращаем null (без клавиатуры).
            default -> {
                return null;
            }
        }
        return markup;
    }
}