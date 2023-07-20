package ru.transport24.bot.service;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import ru.transport24.bot.model.News;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.transport24.bot.repository.NewsRepository;
import ru.transport24.bot.exception.NotFoundException;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsService {
    final NewsRepository newsRepository;

    // Обновляем новости с сайта mintrans.krskstate.ru
    public void parseNews() throws IOException {
        // Получаем страницу новостей с сайта.
        Document newsPage = Jsoup.connect("https://mintrans.krskstate.ru/press/news/").get();
        // Делаем выборку по новостям.
        Elements getNews = newsPage.getElementsByClass("news");
        // Берём последние 5 новостей и сохраняем их в БД.
        for (int i = 0; i < 5; i++) {
            // Получаем ссылку на новость.
            String link = "https://mintrans.krskstate.ru" + getNews.get(i).getElementsByAttribute("href").attr("href");
            // Получаем страницу с новостью.
            Document newsFromSite = Jsoup.connect(link).get();
            // Создаём новость и сохраняем в БД.
            News news = News.builder()
                    .id(i + 1L)
                    .date(newsFromSite.getElementsByClass("date").get(0).text())
                    .title(newsFromSite.title().substring(newsFromSite.title().indexOf("/") + 2))
                    .link(link)
                    .build();
            newsRepository.save(news);
            log.info("\nСохранена новость => " + news);
        }
    }

    // Поиск новости в БД.
    public News getNews(Long newsId) {
        return newsRepository
                .findById(newsId)
                .orElseThrow(() -> new NotFoundException("Новость с ИД: " + newsId + ", не найдена!"));
    }
}