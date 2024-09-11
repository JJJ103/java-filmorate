package ru.yandex.practicum.filmorate.mapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FilmMapper {

    public static Film mapToFilm(FilmRequest request) {
        Film film = new Film();
        film.setName(request.getName());
        film.setDescription(request.getDescription());
        film.setReleaseDate(request.getReleaseDate());
        film.setDuration(request.getDuration());
        // Если нужно, добавьте инициализацию других полей
        return film;
    }

    public static FilmDto mapToFilmDto(Film film) {
        FilmDto dto = new FilmDto();
        dto.setName(film.getName());
        dto.setDescription(film.getDescription());
        dto.setReleaseDate(film.getReleaseDate());
        dto.setDuration(film.getDuration());
        // Если нужно, добавьте маппинг других полей
        return dto;
    }

    // Опционально: если есть метод, который мапит FilmRequest на FilmDto
    public static FilmDto mapToFilmDto(FilmRequest request) {
        FilmDto dto = new FilmDto();
        dto.setName(request.getName());
        dto.setDescription(request.getDescription());
        dto.setReleaseDate(request.getReleaseDate());
        dto.setDuration(request.getDuration());
        return dto;
    }
}
