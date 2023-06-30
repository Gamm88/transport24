package ru.transport24.bot.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class BotConfig {
    // Имя бота, используется для запуска.
    @Value("${bot.username}")
    String botUsername;

    // Tокен бота, используется для запуска.
    @Value("${bot.token}")
    String botToken;

    // Администратор бота.
    @Value("${bot.admin}")
    Long botAdmin;
}