package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Film> filmRowMapper = new FilmMapper();

    private static final String INSERT_QUERY = "INSERT INTO films (name, description, release_date, duration) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ? WHERE id = ?";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM films WHERE id = ?";
    private static final String FIND_ALL_QUERY = " SELECT * FROM films";

    @Override
    public Film addFilm(Film film) {
        jdbcTemplate.update(INSERT_QUERY, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration());
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        jdbcTemplate.update(UPDATE_QUERY, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(), film.getId());
        return film;
    }

    @Override
    public List<Film> getAllFilms() {
        return jdbcTemplate.query(FIND_ALL_QUERY, filmRowMapper);
    }

    @Override
    public Film getFilmById(Long id) {
        return jdbcTemplate.queryForObject(FIND_BY_ID_QUERY, filmRowMapper, id);
    }

    @Override
    public void likeFilm(Long filmId, Long userId) {
        log.info("Попытка лайкнуть фильм с ID {} пользователем с ID {}", filmId, userId);

        String checkLikeQuery = "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?";
        Integer likeCount = jdbcTemplate.queryForObject(checkLikeQuery, Integer.class, filmId, userId);

        if (likeCount != null && likeCount > 0) {
            log.info("Пользователь с ID {} уже лайкнул фильм с ID {}", userId, filmId);
            return;
        }

        // Добавление лайка
        String addLikeQuery = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(addLikeQuery, filmId, userId);

        log.info("Фильм с ID {} был лайкнут пользователем с ID {}", filmId, userId);
    }

    @Override
    public void unlikeFilm(Long filmId, Long userId) {
        // Логика для удаления лайка
    }

    @Override
    public List<Film> getPopularFilms(Integer count) {
        // Логика для получения популярных фильмов
        return null;
    }

    private void validateFilm(Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
    }
}
