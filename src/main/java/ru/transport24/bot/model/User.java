package ru.transport24.bot.model;

import lombok.*;
import jakarta.persistence.*;

import java.util.List;
import java.sql.Timestamp;
import java.util.ArrayList;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table(name = "users")
public class User {
    // Идентификатор пользователя (из телеграмм).
    @Id
    @Column(name = "id")
    private Long id;

    // Имя пользователя.
    @Column(name = "first_name")
    private String firstName;

    // Фамилия пользователя.
    @Column(name = "last_name")
    private String lastName;

    // Никнейм пользователя.
    @Column(name = "user_name")
    private String userName;

    // Дата регистрации (подключения к боту).
    @Column(name = "registered")
    private Timestamp registered;

    // Включены или нет уведомления.
    @Column(name = "notification")
    private Boolean notification;

    // Карты, которые добавил пользователь, параметры:
    // mappedBy = "user" - название объекта у Card;
    // fetch = FetchType.EAGER - помогает избежать ошибки Lazy Initialization;
    // cascade = CascadeType.ALL - выполнение каскадных операций;
    // orphanRemoval = true - если пользователь будет удалён, удалятся его карты.
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Card> cards = new ArrayList<>();

    // Добавление карты в БД.
    public void addCard(Card card){
        this.cards.add(card);
        card.setUser(this);
    }

    // Удаление карты в БД.
    public void removeCard(Card card){
        this.cards.remove(card);
        card.setUser(null);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", userName='" + userName + '\'' +
                '}';
    }
}