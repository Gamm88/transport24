package ru.transport24.bot.repository;

import ru.transport24.bot.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardRepository extends JpaRepository<Card, Long> {
    Card findTopByUserIdAndCardNumber(Long UserId, String cardNumber);
}