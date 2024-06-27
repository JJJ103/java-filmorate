package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;

public interface FilmStorage {
    Film addFilm(Film film);

    Film updateFilm(Film film);

    Collection<Film> getAllFilms();

    Film getFilmById(String id);

    void likeFilm(String filmId, String userId);

    void unlikeFilm(String filmId, String userId);

    List<Film> getPopularFilms(String count);
}
