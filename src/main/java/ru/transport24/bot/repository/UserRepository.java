package ru.transport24.bot.repository;

import ru.transport24.bot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    // Поиск пользователей с включенными уведомлениями.
    List<User> findAllByNotificationIsTrue();
}