package ru.yandex.practicum.filmorate.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@Component
@Repository
@Slf4j
public class UserDbStorage extends BaseRepository<User> implements UserStorage {

    private static final String INSERT_USER_QUERY =
            "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";

    private static final String UPDATE_USER_QUERY =
            "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";

    private static final String FIND_USER_BY_ID_QUERY =
            "SELECT * FROM users WHERE user_id = ?";

    private static final String FIND_ALL_USERS_QUERY =
            "SELECT * FROM users";

    private static final String ADD_FRIEND_QUERY =
            "INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, ?)";

    private static final String UPDATE_FRIENDSHIP_STATUS_QUERY =
            "UPDATE friendships SET status = ? WHERE user_id = ? AND friend_id = ?";

    private static final String REMOVE_FRIEND_QUERY =
            "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";

    private static final String GET_FRIENDS_QUERY =
            "SELECT u.* FROM users u " +
                    "JOIN friendships f ON u.user_id = f.friend_id " +
                    "WHERE f.user_id = ? AND f.status = TRUE";

    private static final String GET_COMMON_FRIENDS_QUERY =
            "SELECT u.* FROM users u " +
                    "JOIN friendships f1 ON u.user_id = f1.friend_id " +
                    "JOIN friendships f2 ON u.user_id = f2.friend_id " +
                    "WHERE f1.user_id = ? AND f2.user_id = ? AND f1.status = TRUE AND f2.status = TRUE";


    public UserDbStorage(JdbcTemplate jdbc, UserRowMapper mapper) {
        super(jdbc, mapper, User.class);
    }

    @Override
    public User addUser(User user) {

        long id = insert(
                INSERT_USER_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday()
        );
        user.setId(id);

        return getUserById(id);
    }

    @Override
    public User updateUser(User user) {
        validateUserExists(user.getId());

        update(
                UPDATE_USER_QUERY,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId()
        );
        return user;
    }

    @Override
    public Collection<User> getAllUsers() {
        List<User> users = findMany(FIND_ALL_USERS_QUERY);

        // Загрузка друзей для каждого пользователя
        for (User user : users) {
            loadFriendsForUser(user);
        }

        return users;
    }

    @Override
    public User getUserById(Long id) {
        User user = findOne(FIND_USER_BY_ID_QUERY, id)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id + " не найден"));

        // Загрузка друзей пользователя
        loadFriendsForUser(user);

        return user;
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        validateUserExists(userId);
        validateUserExists(friendId);

        // Добавляем запись о дружбе с неподтверждённым статусом (по умолчанию FALSE)
        String sqlInsert = "INSERT INTO friendships (user_id, friend_id) VALUES (?, ?)";
        jdbc.update(sqlInsert, userId, friendId);

        // Проверяем, есть ли обратная запись
        String sqlCheckReverse = "SELECT status FROM friendships WHERE user_id = ? AND friend_id = ?";
        List<Boolean> reverseStatuses = jdbc.queryForList(sqlCheckReverse, Boolean.class, friendId, userId);

        if (!reverseStatuses.isEmpty()) {
            // Обновляем статус дружбы в обеих записях на TRUE
            String sqlUpdateStatus = "UPDATE friendships SET status = TRUE WHERE (user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)";
            jdbc.update(sqlUpdateStatus, userId, friendId, friendId, userId);
        }
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        validateUserExists(userId);
        validateUserExists(friendId);

        // Удаляем запись о дружбе
        String sqlDelete = "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";
        jdbc.update(sqlDelete, userId, friendId);

        // Проверяем, был ли статус дружбы подтверждённым у обратной записи
        String sqlCheckReverse = "SELECT status FROM friendships WHERE user_id = ? AND friend_id = ?";
        List<Boolean> reverseStatuses = jdbc.queryForList(sqlCheckReverse, Boolean.class, friendId, userId);

        if (!reverseStatuses.isEmpty() && reverseStatuses.get(0)) {
            // Обновляем статус обратной записи на FALSE
            String sqlUpdateStatus = "UPDATE friendships SET status = FALSE WHERE user_id = ? AND friend_id = ?";
            jdbc.update(sqlUpdateStatus, friendId, userId);
        }
    }

    @Override
    public Collection<User> getFriends(Long userId) {
        validateUserExists(userId);

        String sql = "SELECT u.*, f.status FROM users u " +
                "JOIN friendships f ON u.user_id = f.friend_id " +
                "WHERE f.user_id = ?";
        return jdbc.query(sql, new UserRowMapper(), userId);
    }

    @Override
    public Collection<User> getCommonFriends(Long userId, Long otherUserId) {
        validateUserExists(userId);
        validateUserExists(otherUserId);
        String sql = "SELECT u.* FROM users u " +
                "JOIN friendships f1 ON u.user_id = f1.friend_id " +
                "JOIN friendships f2 ON u.user_id = f2.friend_id " +
                "WHERE f1.user_id = ? AND f2.user_id = ?";
        List<User> commonFriends = jdbc.query(sql, mapper, userId, otherUserId);
        return commonFriends;
    }

    private void validateUserExists(Long userId) {
        findOne(FIND_USER_BY_ID_QUERY, userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с указанным ID не найден"));
    }

    private void loadFriendsForUser(User user) {
        String sql = "SELECT friend_id FROM friendships WHERE user_id = ? AND status = TRUE";
        List<Long> friendIds = jdbc.queryForList(sql, Long.class, user.getId());
        user.setFriends(new HashSet<>(friendIds));
    }
}
