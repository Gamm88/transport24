<a name="readme-top"></a>

<!-- PROJECT LOGO -->
<br />
<div align="center">
  <a href="https://github.com/Gamm88/transport24">
    <img src="images/logo.png" alt="Logo" width="80" height="80">
  </a>

<h3 align="center">Transport24</h3>

  <p align="center">
    Телеграм бот общественного транспорт Красноярского края.
    <br />
    <a href="https://t.me/transport24bot"><strong>Открыть в Telegram »</strong></a>
    <br />
    <br />
    <a href="https://github.com/Gamm88/transport24">Открыть на GitHub</a>
    ·
    <a href="https://github.com/Gamm88/transport24/issues">Сообщить об ошибке</a>
    ·
    <a href="https://github.com/Gamm88/transport24/issues">Задать вопрос</a>
  </p>
</div>


<!-- ABOUT THE PROJECT -->
## О проекте

Телеграм бот был разработан как справочно-информационный ресурс для жителей Красноярского края. Основная тематика - это общественный автобусный транспорт и оплата проезда.

Основные функции:
- найти маршрут, ознакомится с расписанием, купить билет;
- посмотреть движение транспорта в режиме онлайн;
- узнать о деятельности легкового такси;
- проверить и контролировать баланс - Банковской, Транспортной или Социальной карты;
- узнать как вывести банковскую карту из стоп-листа и вернуть деньги за незавершённую поездку;
- найти ответы на часто задаваемые вопросы связанные с безналичной оплатой проезда;
- задать вопрос, направить предложение и жалобу;
- на нестандартные вопросы, ответ даёт ChatGPT 3.5.

### Разработан с помощью:

* Java 17
* Spring
* Maven
* TelegramBots
* Lombok
* Hibernate
* Emoji Java
* OKhttp
* PostgreSQL
* Jsoup


<!-- GETTING STARTED -->
## Запуск бота

Для запуска бота потребуется:

1. Клонировать репозиторий
   ```sh
   git clone https://github.com/Gamm88/transport24.git
   ```
2. Создать бота и получить уникальный Token у "отца" всех ботов в телеграм - [https://t.me/BotFather](https://t.me/BotFather).
3. Заполнить application.properties:
   ```js
   bot.username = 'Имя бота';
   ```
   ```js
   bot.token = 'Токен бота';
   ```
      ```js
   bot.admin = 'Ваш ИД в Телеграм';
   ```
4. Подключить базу данных.

<p align="right">(<a href="#readme-top">Вернуться к началу</a>)</p>