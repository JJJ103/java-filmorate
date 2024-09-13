package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Service
@Slf4j
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;


    @Autowired
    public FilmService(@Qualifier("filmDbStorage")FilmStorage filmStorage, @Qualifier("userDbStorage")UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film addFilm(Film film) {
        log.info("Попытка добавления фильма: {}", film);

        validateFilm(film);

        Film savedFilm = filmStorage.addFilm(film);
        log.info("Фильм добавлен: {}", savedFilm);
        return savedFilm;
    }

    public Film updateFilm(Film film) {
        log.info("Попытка обновления фильма: {}", film);

        validateFilm(film);

        Film updatedFilm = filmStorage.updateFilm(film);
        log.info("Фильм обновлен: {}", updatedFilm);
        return updatedFilm;
    }

    public Collection<Film> getAllFilms() {
        log.info("Запрос на получение всех фильмов");
        return filmStorage.getAllFilms();
    }

    public Film getFilmById(Long id) {
        log.info("Запрос на получение фильма по ID: {}", id);

        Film film = filmStorage.getFilmById(id);
        log.info("Фильм найден: {}", film);
        return film;
    }

    public void likeFilm(Long userId, Long filmId) {
        log.info("Попытка лайкнуть фильм с ID {} пользователем с ID {}", filmId, userId);
        userStorage.getUserById(userId); // Проверка существования пользователя
        filmStorage.getFilmById(filmId); // Проверка существования фильма
        filmStorage.likeFilm(userId, filmId);
        log.info("Фильм с ID {} был лайкнут пользователем с ID {}", filmId, userId);
    }

    public void unlikeFilm(Long userId, Long filmId) {
        log.info("Попытка удаления лайка с фильма с ID {} пользователем с ID {}", filmId, userId);
        filmStorage.unlikeFilm(filmId, userId); // Поменяли местами параметры
        log.info("Лайк пользователя с ID {} был удален с фильма с ID {}", userId, filmId);
    }

    public List<Film> getPopularFilms(Integer count) {
        log.info("Запрос на получение популярных фильмов с count {}", count);

        List<Film> films = filmStorage.getPopularFilms(count);
        log.info("Популярные фильмы: {}", films);
        return films;
    }

    private void validateFilm(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
    }
}
