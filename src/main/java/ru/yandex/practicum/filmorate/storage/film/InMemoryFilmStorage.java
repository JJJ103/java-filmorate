package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();
    private long currentId = 0;

    @Override
    public Film addFilm(Film film) {
        log.info("Попытка добавления фильма: {}", film);
        validateFilm(film);
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Фильм добавлен: {}", film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        log.info("Попытка обновления фильма: {}", film);
        if (!films.containsKey(film.getId())) {
            throw new NotFoundException("Фильм не найден");
        }
        films.put(film.getId(), film);
        log.info("Фильм обновлен: {}", film);
        return film;
    }

    @Override
    public Collection<Film> getAllFilms() {
        log.info("Запрос на получение всех фильмов");
        return films.values();
    }

    @Override
    public Film getFilmById(Long id) {
        log.info("Запрос на получение фильма по ID: {}", id);

        if (!films.containsKey(id)) {
            throw new NotFoundException("Фильм с указанным ID не найден");
        }
        return films.get(id);
    }

    @Override
    public void likeFilm(Long userId, Long filmId) {
        log.info("Попытка лайкнуть фильм с ID {} пользователем с ID {}", filmId, userId);
        Film film = validateAndGetFilm(filmId);

        Set<Long> likedByUsers = film.getLikedByUser();
        if (likedByUsers.contains(userId)) {
            log.info("Пользователь с ID {} уже лайкнул фильм с ID {}", userId, filmId);
            return;
        }
        likedByUsers.add(userId);
        log.info("Фильм с ID {} был лайкнут пользователем с ID {}", filmId, userId);
    }

    @Override
    public void unlikeFilm(Long userId, Long filmId) {
        log.info("Попытка удаления лайка с фильма с ID {} пользователем с ID {}", filmId, userId);
        Film film = validateAndGetFilm(filmId);

        Set<Long> likedByUsers = film.getLikedByUser();
        if (!likedByUsers.contains(userId)) {
            throw new NotFoundException("Лайк не был поставлен");
        }
        likedByUsers.remove(userId);
        log.info("Лайк пользователя с ID {} был удален с фильма с ID {}", userId, filmId);
    }

    private Film validateAndGetFilm(Long filmId) {

        if (!films.containsKey(filmId)) {
            throw new NotFoundException("Фильм с указанным ID не найден");
        }

        return films.get(filmId);
    }

    @Override
    public List<Film> getPopularFilms(Integer count) {
        log.info("Запрос на получение популярных фильмов с count {}", count);

        List<Film> popularFilms = films.values().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikedByUser().size(), f1.getLikedByUser().size()))
                .limit(count)
                .collect(Collectors.toList());
        log.info("Популярные фильмы: {}", popularFilms);
        return popularFilms;
    }

    private long getNextId() {
        return ++currentId;
    }

    private void validateFilm(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
    }
}
