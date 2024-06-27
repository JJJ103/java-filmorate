package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final UserStorage userStorage;
    private final UserService userService;

    public UserController(UserStorage userStorage) {
        this.userStorage = userStorage;
        this.userService = new UserService(userStorage);
    }

    @GetMapping
    public Collection<User> getAll() {
        return userStorage.getAllUsers();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User create(@Valid @RequestBody User user) {
        return userStorage.addUser(user);
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        return userStorage.updateUser(user);
    }

    @GetMapping("/{id}")
    public User getFilmById(@PathVariable String id) {
        return userStorage.getUserById(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable String id, @PathVariable String friendId) {
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable String id, @PathVariable String friendId) {
        userService.removeFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public Collection<User> getFriends(@PathVariable String id) {
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<User> getCommonFriends(@PathVariable String id, @PathVariable String otherId) {
        return userService.getCommonFriends(id, otherId);
    }
}
