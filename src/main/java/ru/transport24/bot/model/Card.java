package ru.transport24.bot.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table(name = "cards")
public class Card {
    // Идентификатор карты (генерируется автоматически).
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // Пользователь, которому принадлежит карта (стыковка через ИД пользователя).
    @ManyToOne(fetch = FetchType.EAGER)
    private User user;

    // Тип карты (банковская, транспортная или социальная).
    @Column(name = "card_type")
    private CardType cardType;

    // Номер карты.
    @Column(name = "card_number")
    private String cardNumber;

    // Для БК - задолженность по карте, для ТК - баланс карты
    @Column(name = "balance")
    private Integer balance;

    // Для СК - базовые поездки
    @Column(name = "base_trips")
    private Integer baseTrips;

    // Для СК - дополнительные поездки
    @Column(name = "dop_trips")
    private Integer dopTrips;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return Objects.equals(id, card.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}