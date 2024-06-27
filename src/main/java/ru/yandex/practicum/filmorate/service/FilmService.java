package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.List;

@Service
public class FilmService {

    private final FilmStorage filmStorage;


    @Autowired
    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public void likeFilm(String userId, String filmId) {
        filmStorage.likeFilm(userId, filmId);
    }

    public void unlikeFilm(String userId, String filmId) {
        filmStorage.unlikeFilm(userId, filmId);
    }

    public List<Film> getPopularFilms(String count) {
        return filmStorage.getPopularFilms(count);
    }
}
