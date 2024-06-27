package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {
    User addUser(User user);
    User updateUser(User user);
    Collection<User> getAllUsers();
    User getUserById(String id);
    void addFriend(String userId, String friendId);
    void removeFriend(String userId, String friendId);
    Collection<User> getFriends(String userId);
    Collection<User> getCommonFriends(String userId, String otherUserId);
}