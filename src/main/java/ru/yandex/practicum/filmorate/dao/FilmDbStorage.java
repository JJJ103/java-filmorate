package ru.yandex.practicum.filmorate.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@Repository
@Slf4j
public class FilmDbStorage extends BaseRepository<Film> implements FilmStorage {

    private static final String INSERT_QUERY =
            "INSERT INTO films (name, description, release_date, duration, rating_id) " +
                    "VALUES (?, ?, ?, ?, ?)";

    private static final String UPDATE_QUERY =
            "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, rating_id = ? " +
                    "WHERE film_id = ?";

    private static final String FIND_BY_ID_QUERY =
            "SELECT f.*, r.name AS rating_name " +
                    "FROM films f " +
                    "LEFT JOIN ratings r ON f.rating_id = r.rating_id " +
                    "WHERE f.film_id = ?";

    private static final String FIND_ALL_QUERY =
            "SELECT f.*, r.name AS rating_name " +
                    "FROM films f " +
                    "LEFT JOIN ratings r ON f.rating_id = r.rating_id";

    private static final String LIKE_FILM_QUERY =
            "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";

    private static final String UNLIKE_FILM_QUERY =
            "DELETE FROM likes WHERE film_id = ? AND user_id = ?";

    private static final String CHECK_LIKE_EXISTS_QUERY = "SELECT EXISTS (SELECT 1 FROM likes WHERE " +
            "film_id = ? AND user_id = ?)";

    private static final String GET_POPULAR_FILMS_QUERY =
            "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.rating_id, " +
                    "r.name AS rating_name, COUNT(l.user_id) AS likes " +
                    "FROM films f " +
                    "LEFT JOIN likes l ON f.film_id = l.film_id " +
                    "LEFT JOIN ratings r ON f.rating_id = r.rating_id " +
                    "GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration, f.rating_id, r.name " +
                    "ORDER BY likes DESC " +
                    "LIMIT ?";


    public FilmDbStorage(JdbcTemplate jdbc, FilmRowMapper mapper) {
        super(jdbc, mapper, Film.class);
    }

    @Override
    public Film addFilm(Film film) {
        validateMPA(film.getMpa());
        validateGenres(film.getGenres());

        long filmId = insert(
                INSERT_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId()
        );
        film.setId(filmId);

        // Сохранение жанров
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            String genreSql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
            for (Genre genre : film.getGenres()) {
                jdbc.update(genreSql, filmId, genre.getId());
            }
        }

        return getFilmById(filmId);
    }

    @Override
    public Film updateFilm(Film film) {
        validateFilm(film.getId());
        validateMPA(film.getMpa());
        validateGenres(film.getGenres());

        update(
                UPDATE_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );

        // Удаляем старые жанры
        String deleteGenresSql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbc.update(deleteGenresSql, film.getId());

        // Добавляем новые жанры
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            String genreSql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
            for (Genre genre : film.getGenres()) {
                jdbc.update(genreSql, film.getId(), genre.getId());
            }
        }

        return getFilmById(film.getId());
    }

    @Override
    public List<Film> getAllFilms() {
        List<Film> films = findMany(FIND_ALL_QUERY);

        // Загрузка жанров лайков
        for (Film film : films) {
            loadGenresForFilm(film);
            loadLikesForFilm(film);
        }

        return films;
    }

    @Override
    public Film getFilmById(Long id) {
        String sql = "SELECT f.*, r.name AS rating_name " +
                "FROM films f " +
                "LEFT JOIN ratings r ON f.rating_id = r.rating_id " +
                "WHERE f.film_id = ?";
        Film film = findOne(sql, id)
                .orElseThrow(() -> new ValidationException("Фильм с указанным ID не найден"));

        // Загрузка жанров лайков
        loadGenresForFilm(film);
        loadLikesForFilm(film);

        return film;
    }

    @Override
    public void likeFilm(Long filmId, Long userId) {
        validateFilm(filmId);

        // Проверяем, лайкнул ли уже пользователь фильм
        Boolean exists = jdbc.queryForObject(CHECK_LIKE_EXISTS_QUERY, Boolean.class, filmId, userId);

        if (Boolean.TRUE.equals(exists)) {
            log.info("Пользователь с ID {} уже лайкнул фильм с ID {}", userId, filmId);
            return;
        }

        jdbc.update(LIKE_FILM_QUERY, filmId, userId);
    }

    @Override
    public void unlikeFilm(Long filmId, Long userId) {
        validateFilm(filmId);
        Boolean exists = jdbc.queryForObject(CHECK_LIKE_EXISTS_QUERY, Boolean.class, filmId, userId);

        if (Boolean.FALSE.equals(exists)) {
            throw new NotFoundException("Лайк не был поставлен");
        }

        jdbc.update(UNLIKE_FILM_QUERY, filmId, userId);
    }

    @Override
    public List<Film> getPopularFilms(Integer count) {
        List<Film> films = jdbc.query(GET_POPULAR_FILMS_QUERY, mapper, count);

        for (Film film : films) {
            loadGenresForFilm(film);
            loadLikesForFilm(film);
        }

        return films;
    }

    private void loadLikesForFilm(Film film) {
        String sql = "SELECT user_id FROM likes WHERE film_id = ?";
        List<Long> likedByUserIds = jdbc.queryForList(sql, Long.class, film.getId());
        Set<Long> likedByUserSet = new HashSet<>(likedByUserIds);
        film.setLikedByUser(likedByUserSet);
    }

    private void loadGenresForFilm(Film film) {
        String genresSql = "SELECT g.genre_id, g.name " +
                "FROM film_genres fg " +
                "JOIN genres g ON fg.genre_id = g.genre_id " +
                "WHERE fg.film_id = ? ";
        List<Genre> genres = jdbc.query(genresSql, (rs, rowNum) -> {
            Genre genre = new Genre();
            genre.setId(rs.getInt("genre_id"));
            genre.setName(rs.getString("name"));
            return genre;
        }, film.getId());
        film.setGenres(genres);
    }

    private void validateFilm(Long filmId) {
        findOne(FIND_BY_ID_QUERY, filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с указанным ID не найден"));
    }

    private void validateMPA(MPA mpa) {
        String sql = "SELECT COUNT(*) FROM ratings WHERE rating_id = ?";
        Integer count = jdbc.queryForObject(sql, Integer.class, mpa.getId());
        if (count == null || count == 0) {
            throw new ValidationException("Рейтинг MPA с таким ID не существует");
        }
    }
    private void validateGenres(List<Genre> genres) {
        if (genres == null) return;
        for (Genre genre : genres) {
            String sql = "SELECT COUNT(*) FROM genres WHERE genre_id = ?";
            Integer count = jdbc.queryForObject(sql, Integer.class, genre.getId());
            if (count == null || count == 0) {
                throw new ValidationException("Жанр с ID " + genre.getId() + " не существует");
            }
        }
    }
}
