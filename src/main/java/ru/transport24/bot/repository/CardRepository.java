package ru.transport24.bot.repository;

import ru.transport24.bot.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CardRepository extends JpaRepository<Card, Long> {
    // Поиск карт по ИД пользователя.
    List<Card> findAllByUserId(Long UserId);
    // Поиск карт по ИД пользователя и номеру.
    Card findByUserIdAndCardNumber(Long UserId, String cardNumber);

    Card findTopByUserIdAndCardNumber(Long UserId, String cardNumber);
}