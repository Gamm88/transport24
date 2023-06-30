package ru.transport24.bot.service;

import lombok.extern.slf4j.Slf4j;
import ru.transport24.bot.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.transport24.bot.repository.UserRepository;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.sql.Timestamp;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    final UserRepository userRepository;

    // Добавление пользователя.
    public User addUser(Update update) {
        // Получаем ИД пользователя.
        Long id = update.getMessage().getChatId();
        // Если пользователь ещё не зарегистрирован, добавляем его в БД.
        User user = getUser(id);
        if (user == null) {
            user = User.builder()
                    .id(id)
                    .firstName(update.getMessage().getChat().getFirstName())
                    .lastName(update.getMessage().getChat().getLastName())
                    .userName(update.getMessage().getChat().getUserName())
                    .registered(new Timestamp(System.currentTimeMillis()))
                    .notification(true)
                    .build();
            userRepository.save(user);
            log.info("Добавлен новый пользователь: " + user);
        }
        // Возвращаем пользователя, нового или уже имеющегося.
        return user;
    }

    // Удаление пользователя.
    public String deleteUser(Long chatId) {
        // Если пользователь зарегистрирован, удаляем его из БД.
        if (getUser(chatId) != null) {
            userRepository.deleteById(chatId);
            log.info("Удалён пользователь с ИД: " + chatId);
            return "Регистрация удалена!";
        } else {
            // Если не зарегистрирован, то сообщаем об этом и нечего не делаем.
            return "Удаление регистрации невозможно,\nВы не зарегистрированы!";
        }
    }

    // Поиск всех пользователей, которые подписаны на рассылку.
    public List<User> getUsersByNotificationIsTrue() {
        return userRepository.findAllByNotificationIsTrue();
    }

    // Поиск пользователя по ИД.
    public User getUser(Long chatId) {
        return userRepository
                .findById(chatId)
                .orElse(null);
    }
}