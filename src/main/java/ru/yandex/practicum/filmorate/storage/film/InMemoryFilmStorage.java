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
        validateFilm(film);
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
    public Film getFilmById(String id) {
        if (!id.matches("\\d+")) {
            throw new ValidationException("ID должен быть числом");
        }
        long filmId = Long.parseLong(id);

        if (filmId <= 0) {
            throw new ValidationException("ID должен быть больше нуля");
        }
        if (!films.containsKey(filmId)) {
            throw new NotFoundException("Фильм с указанным ID не найден");
        }
        return films.get(filmId);
    }

    @Override
    public void likeFilm(String userIdStr, String filmIdStr) {
        Film film = validateAndGetFilm(filmIdStr, userIdStr);
        long userId = Long.parseLong(userIdStr);

        Set<Long> likedByUsers = film.getLikedByUser();
        if (likedByUsers.contains(userId)) {
            return;
        }
        likedByUsers.add(userId);
    }

    @Override
    public void unlikeFilm(String userIdStr, String filmIdStr) {
        Film film = validateAndGetFilm(filmIdStr, userIdStr);
        long userId = Long.parseLong(userIdStr);

        Set<Long> likedByUsers = film.getLikedByUser();
        if (!likedByUsers.contains(userId)) {
            throw new NotFoundException("Лайк не был поставлен");
        }
        likedByUsers.remove(userId);
    }

    private Film validateAndGetFilm(String filmIdStr, String userIdStr) {
        if (!filmIdStr.matches("\\d+") || !userIdStr.matches("\\d+")) {
            throw new ValidationException("ID должен быть числом");
        }
        long filmId = Long.parseLong(filmIdStr);
        long userId = Long.parseLong(userIdStr);

        if (filmId <= 0 || userId <= 0) {
            throw new ValidationException("ID должен быть больше нуля");
        }
        if (!films.containsKey(filmId)) {
            throw new NotFoundException("Фильм с указанным ID не найден");
        }

        return films.get(filmId);
    }

    @Override
    public List<Film> getPopularFilms(String countStr) {
        int count;
        try {
            count = Integer.parseInt(countStr);
        } catch (NumberFormatException e) {
            throw new ValidationException("Параметр count должен быть числом");
        }

        if (count <= 0) {
            throw new ValidationException("Параметр count должен быть больше нуля");
        }

        return films.values().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikedByUser().size(), f1.getLikedByUser().size()))
                .limit(count)
                .collect(Collectors.toList());
    }

    private long getNextId() {
        return ++currentId;
    }

    private void validateFilm(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.error("Ошибка валидации: дата релиза не может быть раньше 28 декабря 1895 года");
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
    }
}
