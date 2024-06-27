package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ServerErrorException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private long currentId = 0;

    @Override
    public User addUser(User user) {
        validateUser(user);
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Создан пользователь: {}", user);
        return user;
    }

    @Override
    public User updateUser(User user) {
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
        return users.values();
    }

    private long getNextId() {
        return ++currentId;
    }

    @Override
    public User getUserById (String id) {
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
        return Long.parseLong(idStr);
    }

    @Override
    public void addFriend(String userIdStr, String friendIdStr) {
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
    }

    @Override
    public void removeFriend(String userIdStr, String friendIdStr) {
        User user = getUserById(userIdStr);
        User friendUser = getUserById(friendIdStr);

        user.getFriends().remove(friendUser.getId());
        friendUser.getFriends().remove(user.getId());
    }

    @Override
    public Collection<User> getFriends(String userIdStr) {
        User user = getUserById(userIdStr);

        Set<Long> friendIds = user.getFriends();
        List<User> friends = new ArrayList<>();

        for (Long friendId : friendIds) {
            User friend = getUserById(String.valueOf(friendId));
            friends.add(friend);
        }
        return friends;
    }

    @Override
    public List<User> getCommonFriends(String userId, String otherId) {
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

        return commonFriends;
    }

    private void validateUser(User user) {
        if (user == null) {
            throw new ServerErrorException("Внутренняя ошибка сервера");
        }
        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());
        }
    }

    //вспомогательный метод
    public boolean containsUserById(Long userId) {
        return users.containsKey(userId);
    }
}
