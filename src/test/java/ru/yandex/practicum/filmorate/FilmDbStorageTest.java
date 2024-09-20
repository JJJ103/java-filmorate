package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import ru.yandex.practicum.filmorate.dao.FilmDbStorage;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MPA;
import java.time.LocalDate;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dao.mappers.FilmRowMapper;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class, FilmRowMapper.class})
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;

    @Test
    public void testAddFilm() {
        Film newFilm = new Film();
        newFilm.setName("New Film");
        newFilm.setDescription("New Film Description");
        newFilm.setReleaseDate(LocalDate.of(2022, 1, 1));
        newFilm.setDuration(150);
        newFilm.setMpa(new MPA(1, "G"));

        Film addedFilm = filmStorage.addFilm(newFilm);

        assertThat(addedFilm).isNotNull();
        assertThat(addedFilm.getId()).isNotNull();
        assertThat(addedFilm.getName()).isEqualTo("New Film");
    }

    @Test
    public void testUpdateFilm() {
        Film film = filmStorage.getFilmById(1L);
        film.setName("Updated Film");

        Film updatedFilm = filmStorage.updateFilm(film);

        assertThat(updatedFilm.getName()).isEqualTo("Updated Film");
    }

    @Test
    public void testGetAllFilms() {
        Collection<Film> films = filmStorage.getAllFilms();

        assertThat(films).hasSize(2);
    }

    @Test
    public void testFindFilmById() {
        Optional<Film> filmOptional = Optional.ofNullable(filmStorage.getFilmById(1L));

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(film -> {
                    assertThat(film.getId()).isEqualTo(1L);
                });
    }

    @Test
    public void testLikeFilm() {
        filmStorage.likeFilm(3L, 1L);

        Film film = filmStorage.getFilmById(1L);
        assertThat(film.getLikedByUser()).contains(3L);
    }

    @Test
    public void testUnlikeFilm() {
        filmStorage.unlikeFilm(1L, 1L);

        Film film = filmStorage.getFilmById(1L);
        assertThat(film.getLikedByUser()).doesNotContain(1L);
    }

    @Test
    public void testGetPopularFilms() {
        List<Film> popularFilms = filmStorage.getPopularFilms(1);

        assertThat(popularFilms).hasSize(1);
        assertThat(popularFilms.get(0).getName()).isEqualTo("Film One");
    }
}
