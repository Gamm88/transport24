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
import java.util.List;

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
            log.info("\nСохранена новость => " + news.toString());
        }
    }

    /*
    // Добавить новость в БД.
    News addNews(String text) {
        // Создаём новость.
        News news = new News();
        // Заголовок - в квадратных скобках [заголовок].
        news.setTitle(text.substring(text.indexOf("[") + 1, text.indexOf("]")));
        // Содержание - в треугольных скобках <ссылка>.
        news.setContent(text.substring(text.indexOf("<") + 1, text.indexOf(">")));
        // Ссылка - в фигурных скобках {ссылка}.
        news.setLink(text.substring(text.indexOf("{") + 1, text.indexOf("}")));
        // Путь хранения фотографии.
        news.setPhoto("C:/Transport24/Files/" + news.getId() + ".png");
        // Сохраняем и возвращаем новость.
        newsRepository.save(news);
        log.info("Добавлена новость: " + news);
        return news;
    }

     */

    // Удалить новость из БД.
    void deleteNews(String text) {
        // ИД новости - в квадратных скобках [ИД].
        Long newsID = Long.valueOf(text.substring(text.indexOf("[") + 1, text.indexOf("]")));
        // Проверяем наличие новость с таким ИД.
        News news = getNews(newsID);
        // Удаляем новость.
        newsRepository.deleteById(newsID);
        log.info("Удалена новость: " + news);
    }

    // Поиск новости в БД.
    public News getNews(Long newsId) {
        return newsRepository
                .findById(newsId)
                .orElseThrow(() -> new NotFoundException("Новость с ИД: " + newsId + ", не найдена!"));
    }

    // Поиск топ 3 новости в БД.
    public List<News> getTop3News() {
        return newsRepository.findTop3ByOrderByIdDesc();
    }
}