package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {
    private long id;

    @Email(message = "Некорректная электронная почта")
    @NotBlank(message = "Электронная почта не может быть пустой")
    private String email;

    @NotBlank(message = "Логин не может быть пустым")
    //строка не должна содержать пробелов и должна содержать по крайней мере один непробельный символ
    @Pattern(regexp = "\\S+", message = "Логин не может содержать пробелы")
    private String login;

    private String name;

    private Set<Long> friends = new HashSet<>();

    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;

    public void addFriend(Long id) {
        friends.add(id);
    }
}
