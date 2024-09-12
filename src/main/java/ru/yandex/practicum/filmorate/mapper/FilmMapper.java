package ru.yandex.practicum.filmorate.mapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FilmMapper {

    public static Film mapToFilm(NewFilmRequest request) {
        Film film = new Film();
        film.setName(request.getName());
        film.setDescription(request.getDescription());
        film.setReleaseDate(request.getReleaseDate());
        film.setDuration(request.getDuration());


        return film;
    }

    public static FilmDto mapToFilmDto(Film savedFilm) {
        FilmDto dto = new FilmDto();
        dto.setName(savedFilm.getName());
        dto.setDescription(savedFilm.getDescription());
        dto.setReleaseDate(savedFilm.getReleaseDate());
        dto.setDuration(savedFilm.getDuration());

        return dto;
    }
}

