package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;

public interface FilmStorage {
    Film addFilm(Film film);

    Film updateFilm(Film film);

    Collection<Film> getAllFilms();

    Film getFilmById(Long id);

    void likeFilm(Long filmId, Long userId);

    void unlikeFilm(Long filmId, Long userId);

    List<Film> getPopularFilms(Integer count);
}
