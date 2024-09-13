package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;

@Service
@Slf4j
public class UserService {

    private final UserStorage userStorage;

    @Autowired
    public UserService(@Qualifier("userDbStorage")UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> getAllUsers() {
        log.info("Запрос на получение всех пользователей");
        return userStorage.getAllUsers();
    }

    public User addUser(User user) {
        log.info("Попытка добавления пользователя: {}", user);
        User savedUser = userStorage.addUser(user);
        log.info("Создан пользователь: {}", savedUser);
        return savedUser;
    }

    public User updateUser(User user) {
        log.info("Попытка обновления пользователя: {}", user);
        User updatedUser = userStorage.updateUser(user);
        log.info("Пользователь обновлен: {}", updatedUser);
        return updatedUser;
    }

    public User getUserById(Long id) {
        log.info("Запрос на получение пользователя по ID: {}", id);
        User user = userStorage.getUserById(id);
        log.info("Пользватель найден: {}", user);
        return user;
    }

    public void addFriend(Long userId, Long friendId) {
        log.info("Попытка добавления в друзья пользователем с ID {} пользователя с ID {}", userId, friendId);
        userStorage.addFriend(userId, friendId);
        log.info("Пользователи с ID {} и {} стали друзьями", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        log.info("Попытка удаления из друзей пользователем с ID {} пользователя с ID {}", userId, friendId);
        userStorage.removeFriend(userId, friendId);
        log.info("Пользователи с ID {} и {} больше не друзья", userId, friendId);
    }

    public Collection<User> getFriends(Long userId) {
        log.info("Запрос на получение друзей пользователя с ID {}", userId);
        Collection<User> friends = userStorage.getFriends(userId);
        log.info("Пользователь с ID {} имеет друзей: {}", userId, friends);
        return friends;
    }

    public Collection<User> getCommonFriends(Long userId, Long otherId) {
        log.info("Запрос на получение общих друзей пользователей с ID {} и {}", userId, otherId);
        Collection<User> commonFriends = userStorage.getCommonFriends(userId, otherId);
        log.info("Общие друзья пользователей с ID {} и {}: {}", userId, otherId, commonFriends);
        return commonFriends;
    }
}
