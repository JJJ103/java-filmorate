package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ServerErrorException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();
    private long currentId = 0;

    @Override
    public User addUser(User user) {
        log.info("Попытка добавления пользователя: {}", user);
        validateUser(user);
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Создан пользователь: {}", user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        log.info("Попытка обновления пользователя: {}", user);
        validateUser(user);
        if (!users.containsKey(user.getId())) {
            throw new NotFoundException("Ошибка: пользователь не найден");
        }
        users.put(user.getId(), user);
        log.info("Пользователь обновлен: {}", user);
        return user;
    }

    @Override
    public Collection<User> getAllUsers() {
        log.info("Запрос на получение всех пользователей");
        return users.values();
    }

    private long getNextId() {
        return ++currentId;
    }

    @Override
    public User getUserById(Long id) {
        log.info("Запрос на получение пользователя по ID: {}", id);
        long userId = validUserId(id);

        return users.get(userId);
    }

    private long validUserId(Long id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("Пользователь с указанным ID не найден");
        }
        return id;
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        log.info("Попытка добавления в друзья пользователем с ID {} пользователя с ID {}", userId, friendId);
        User user = getUserById(userId);
        User friendUser = getUserById(friendId);

        if (user.getId() == friendUser.getId()) {
            throw new ValidationException("Невозможно добавить самого себя в друзья");
        }
        if (user.getFriends() == null & friendUser.getFriends() == null) {
            user.addFriend(friendUser.getId());
            friendUser.addFriend(user.getId());
            return;
        }
        if (user.getFriends().contains(friendUser.getId())) {
            throw new ValidationException("Пользователь уже является другом");
        }

        user.addFriend(friendUser.getId());
        friendUser.addFriend(user.getId());
        log.info("Пользователи с ID {} и {} стали друзьями", userId, friendId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        log.info("Попытка удаления из друзей пользователем с ID {} пользователя с ID {}", userId, friendId);
        User user = getUserById(userId);
        User friendUser = getUserById(friendId);

        user.getFriends().remove(friendUser.getId());
        friendUser.getFriends().remove(user.getId());
        log.info("Пользователи с ID {} и {} больше не друзья", userId, friendId);
    }

    @Override
    public Collection<User> getFriends(Long userId) {
        log.info("Запрос на получение друзей пользователя с ID {}", userId);
        User user = getUserById(userId);

        Set<Long> friendIds = user.getFriends();
        List<User> friends = new ArrayList<>();

        for (Long friendId : friendIds) {
            User friend = getUserById(friendId);
            friends.add(friend);
        }
        log.info("Пользователь с ID {} имеет друзей: {}", userId, friends);
        return friends;
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long otherId) {
        log.info("Запрос на получение общих друзей пользователей с ID {} и {}", userId, otherId);
        User user = getUserById(userId);
        User otherUser = getUserById(otherId);

        Set<Long> userFriends = user.getFriends();
        Set<Long> otherUserFriends = otherUser.getFriends();

        // Находим общих друзей
        List<Long> commonFriendsId = userFriends.stream()
                .filter(otherUserFriends::contains)
                .toList();

        List<User> commonFriends = new ArrayList<>();
        for (Long id : commonFriendsId) {
            commonFriends.add(users.get(id));
        }

        log.info("Общие друзья пользователей с ID {} и {}: {}", userId, otherId, commonFriends);
        return commonFriends;
    }

    private void validateUser(User user) {

        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());
            log.info("Установлено имя пользователя: {}", user.getName());
        }
    }
}
