package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    private final FilmStorage filmStorage;
    private final FilmService filmService;
    private final UserStorage userStorage;

    public FilmController(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.filmService = new FilmService(filmStorage);
        this.userStorage = userStorage;
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        return filmStorage.addFilm(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        return filmStorage.updateFilm(film);
    }

    @GetMapping
    public Collection<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable String id) {
        return filmStorage.getFilmById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void likeFilm(@PathVariable String userId, @PathVariable String id) {
        if (userStorage.getUserById(userId) == null) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }
        filmService.likeFilm(userId, id);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void unlikeFilm(@PathVariable String userId, @PathVariable String id) {
        filmService.unlikeFilm(userId, id);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10") String count) {
        return filmService.getPopularFilms(count);
    }
}
