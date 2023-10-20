package ru.transport24.bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.transport24.bot.model.News;

public interface NewsRepository extends JpaRepository<News, Long> {
}