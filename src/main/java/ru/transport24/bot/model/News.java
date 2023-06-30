package ru.transport24.bot.model;

import lombok.*;
import jakarta.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Table(name = "news")
public class News {
    // Идентификатор.
    @Id
    @Column(name = "id")
    private Long id;

    // Заголовок.
    @Column(name = "title")
    private String title;

    // Дата публикации.
    @Column(name = "date")
    private String date;

    // Ссылка на сайт минтранса.
    @Column(name = "link")
    private String link;

    @Override
    public String toString() {
        return "News{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", date='" + date + '\'' +
                ", link='" + link + '\'' +
                '}';
    }
}