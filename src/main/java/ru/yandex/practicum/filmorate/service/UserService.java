package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;

@Service
public class UserService {

    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addFriend(String userId, String friendId) {
        userStorage.addFriend(userId, friendId);
    }

    public void removeFriend(String userId, String friendId) {
        userStorage.removeFriend(userId, friendId);
    }

    public Collection<User> getFriends(String userId) {
        return userStorage.getFriends(userId);
    }

    public Collection<User> getCommonFriends(String userId, String otherId) {
        return userStorage.getCommonFriends(userId, otherId);
    }
}
