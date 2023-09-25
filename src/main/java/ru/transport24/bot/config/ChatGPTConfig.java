package ru.transport24.bot.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class ChatGPTConfig {
    // Имя бота, используется для запуска.
    @Value("${GPT.url}")
    String GPTUrl;

    // Tокен бота, используется для запуска.
    @Value("${GPT.apiKey}")
    String GPTApiKey;

    // Администратор бота.
    @Value("${GPT.model}")
    String GPTModel;
}
