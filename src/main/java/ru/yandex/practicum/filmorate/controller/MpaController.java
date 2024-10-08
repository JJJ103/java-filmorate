package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.MPA;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.Collection;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaController {

    private final MpaService mpaService;

    @GetMapping
    public Collection<MPA> getAllMpa() {
        return mpaService.getAllMpa();
    }

    @GetMapping("/{id}")
    public MPA getMpaById(@PathVariable @Positive int id) {
        return mpaService.getMpaById(id);
    }
}
