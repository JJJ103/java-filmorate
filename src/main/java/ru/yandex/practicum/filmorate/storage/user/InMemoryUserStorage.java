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
    public User getUserById(String id) {
        log.info("Запрос на получение пользователя по ID: {}", id);
        long userId = validUserIdToLong(id);

        return users.get(userId);
    }

    private long validUserIdToLong(String idStr) {
        if (!idStr.matches("\\d+")) {
            throw new ValidationException("ID должен быть числом");
        }
        long userId = Long.parseLong(idStr);

        if (userId <= 0) {
            throw new ValidationException("ID должен быть больше нуля");
        }
        if (!users.containsKey(userId)) {
            throw new NotFoundException("Пользователь с указанным ID не найден");
        }
        return userId;
    }

    @Override
    public void addFriend(String userIdStr, String friendIdStr) {
        log.info("Попытка добавления в друзья пользователем с ID {} пользователя с ID {}", userIdStr, friendIdStr);
        User user = getUserById(userIdStr);
        User friendUser = getUserById(friendIdStr);

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
        log.info("Пользователи с ID {} и {} стали друзьями", userIdStr, friendIdStr);
    }

    @Override
    public void removeFriend(String userIdStr, String friendIdStr) {
        log.info("Попытка удаления из друзей пользователем с ID {} пользователя с ID {}", userIdStr, friendIdStr);
        User user = getUserById(userIdStr);
        User friendUser = getUserById(friendIdStr);

        user.getFriends().remove(friendUser.getId());
        friendUser.getFriends().remove(user.getId());
        log.info("Пользователи с ID {} и {} больше не друзья", userIdStr, friendIdStr);
    }

    @Override
    public Collection<User> getFriends(String userIdStr) {
        log.info("Запрос на получение друзей пользователя с ID {}", userIdStr);
        User user = getUserById(userIdStr);

        Set<Long> friendIds = user.getFriends();
        List<User> friends = new ArrayList<>();

        for (Long friendId : friendIds) {
            User friend = getUserById(String.valueOf(friendId));
            friends.add(friend);
        }
        log.info("Пользователь с ID {} имеет друзей: {}", userIdStr, friends);
        return friends;
    }

    @Override
    public List<User> getCommonFriends(String userId, String otherId) {
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
        if (user == null) {
            throw new ServerErrorException("Внутренняя ошибка сервера");
        }
        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());
            log.info("Установлено имя пользователя: {}", user.getName());
        }
    }
}
