package ru.yandex.practicum.filmorate.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.List;

@Component
@Repository
@Slf4j
public class FilmDbStorage extends BaseRepository<Film> implements FilmStorage {

    private static final String INSERT_QUERY =
            "INSERT INTO films (name, description, release_date, duration) " +
                    "VALUES (?, ?, ?, ?)";

    private static final String UPDATE_QUERY =
            "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ? " +
                    "WHERE id = ?";

    private static final String FIND_BY_ID_QUERY =
            "SELECT * FROM films WHERE id = ?";

    private static final String FIND_ALL_QUERY =
            "SELECT * FROM films";

    private static final String LIKE_FILM_QUERY =
            "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";

    private static final String UNLIKE_FILM_QUERY =
            "DELETE FROM likes WHERE film_id = ? AND user_id = ?";

    private static final String CHECK_LIKE_QUERY =
            "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?";

    private static final String GET_POPULAR_FILMS_QUERY =
            "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, COUNT(l.user_id) AS likes " +
                    "FROM films f " +
                    "LEFT JOIN likes l ON f.film_id = l.film_id " +
                    "GROUP BY f.film_id " +
                    "ORDER BY likes DESC " +
                    "LIMIT ?";

    public FilmDbStorage(JdbcTemplate jdbc, FilmRowMapper mapper) {
        super(jdbc, mapper, Film.class);
    }

    @Override
    public Film addFilm(Film film) {
        long id = insert(
                INSERT_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration()
        );
        film.setId(id);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        validateFilm(film.getId());

        update(
                UPDATE_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getId()
        );

        return film;
    }

    @Override
    public List<Film> getAllFilms() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Film getFilmById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id)
                .orElseThrow(() -> new ValidationException("Фильм с указанным ID не найден"));
    }

    @Override
    public void likeFilm(Long filmId, Long userId) {
        validateFilm(filmId);

        // Проверяем, лайкнул ли уже пользователь фильм
        Integer count = jdbc.queryForObject(CHECK_LIKE_QUERY, Integer.class, filmId, userId);

        if (count != null && count > 0) {
            log.info("Пользователь с ID {} уже лайкнул фильм с ID {}", userId, filmId);
            return;
        }

        jdbc.update(LIKE_FILM_QUERY, filmId, userId);
    }

    @Override
    public void unlikeFilm(Long filmId, Long userId) {
        validateFilm(filmId);
        Integer count = jdbc.queryForObject(CHECK_LIKE_QUERY, Integer.class, filmId, userId);

        if (count == null || count == 0) {
            throw new NotFoundException("Лайк не был поставлен");
        }

        jdbc.update(UNLIKE_FILM_QUERY, filmId, userId);
    }

    @Override
    public List<Film> getPopularFilms(Integer count) {
        return jdbc.query(GET_POPULAR_FILMS_QUERY, mapper, count);
    }

    private void validateFilm(Long filmId) {
        findOne(FIND_BY_ID_QUERY, filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с указанным ID не найден"));
    }
}
