package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
@SpringBootTest
public class FilmTests {

    private final Validator validator;

    public FilmTests() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testValidFilm() {
        Film film = new Film();
        film.setName("Film Name");
        film.setDescription("Film description");
        film.setReleaseDate(LocalDate.of(1990, 1, 1));
        film.setDuration(120);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(0, violations.size());
    }

    @Test
    public void testEmptyName() {
        Film film = new Film();
        film.setName("");
        film.setDescription("Film description");
        film.setReleaseDate(LocalDate.of(1990, 1, 1));
        film.setDuration(120);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size());
        assertEquals("Название не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    public void testTooLongDescription() {
        Film film = new Film();
        film.setName("Film Name");
        film.setDescription("This description is too long".repeat(10));
        film.setReleaseDate(LocalDate.of(1990, 1, 1));
        film.setDuration(120);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size());
        assertEquals("Максимальная длина описания — 200 символов", violations.iterator().next().getMessage());
    }

    @Test
    public void testNegativeDuration() {
        Film film = new Film();
        film.setName("Film Name");
        film.setDescription("Film description");
        film.setReleaseDate(LocalDate.of(1990, 1, 1));
        film.setDuration(-120);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertEquals(1, violations.size());
        assertEquals("Продолжительность фильма должна быть положительным числом", violations.iterator().next().getMessage());
    }
}