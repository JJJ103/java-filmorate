package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();
    private long currentId = 0;

    @Override
    public Film addFilm(Film film) {
        film.setId(getNextId());
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        if (!films.containsKey(film.getId())) {
            throw new NotFoundException("Фильм не найден");
        }
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Collection<Film> getAllFilms() {
        return films.values();
    }

    @Override
    public Film getFilmById(Long id) {
        if (!films.containsKey(id)) {
            throw new NotFoundException("Фильм с указанным ID не найден");
        }
        return films.get(id);
    }

    @Override
    public void likeFilm(Long userId, Long filmId) {
        Film film = validateAndGetFilm(filmId);

        Set<Long> likedByUsers = film.getLikedByUser();
        if (likedByUsers.contains(userId)) {
            log.info("Пользователь с ID {} уже лайкнул фильм с ID {}", userId, filmId);
            return;
        }
        likedByUsers.add(userId);
    }

    @Override
    public void unlikeFilm(Long userId, Long filmId) {
        Film film = validateAndGetFilm(filmId);

        Set<Long> likedByUsers = film.getLikedByUser();
        if (!likedByUsers.contains(userId)) {
            throw new NotFoundException("Лайк не был поставлен");
        }
        likedByUsers.remove(userId);
    }

    private Film validateAndGetFilm(Long filmId) {

        if (!films.containsKey(filmId)) {
            throw new NotFoundException("Фильм с указанным ID не найден");
        }

        return films.get(filmId);
    }

    @Override
    public List<Film> getPopularFilms(Integer count) {

        List<Film> popularFilms = films.values().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikedByUser().size(), f1.getLikedByUser().size()))
                .limit(count)
                .collect(Collectors.toList());
        return popularFilms;
    }

    private long getNextId() {
        return ++currentId;
    }
}
