package ru.transport24.bot.model;

// Тип обновления
public enum MessageType {
    COMMAND, // команда из меню
    BUTTON,  // нажатие кнопки
    BALANCE, // проверка баланса
    NEWS,    // новость
    OTHER    // прочее
}