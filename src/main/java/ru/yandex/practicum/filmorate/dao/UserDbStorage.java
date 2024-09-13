package ru.yandex.practicum.filmorate.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;

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

    private static final String REMOVE_FRIEND_QUERY =
            "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";

    private static final String GET_FRIENDS_QUERY =
            "SELECT u.* FROM users u JOIN friendships f ON u.user_id = f.friend_id WHERE f.user_id = ?";

    private static final String GET_COMMON_FRIENDS_QUERY =
            "SELECT u.*" +
                    "FROM users u" +
                    "JOIN friendships f ON u.user_id = f.friend_id " +
                    "WHERE f.user_id = ? AND f.status = true";

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

        log.info("Пользователь с ID {} успешно добавлен", id);
        return getUserById(id);
    }

    @Override
    public User updateUser(User user) {
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
        return findMany(FIND_ALL_USERS_QUERY);
    }

    @Override
    public User getUserById(Long id) {
        return findOne(FIND_USER_BY_ID_QUERY, id)
                .orElseThrow(() -> new ValidationException("Пользователь с таким ID не найден"));
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        jdbc.update(ADD_FRIEND_QUERY, userId, friendId, true); // статус true означает подтвержденную дружбу
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        jdbc.update(REMOVE_FRIEND_QUERY, userId, friendId);
    }

    @Override
    public Collection<User> getFriends(Long userId) {
        String GET_FRIENDS_QUERY = "SELECT * FROM users u " +
                "JOIN friendships f ON u.user_id = f.friend_id " +
                "WHERE f.user_id = ? AND f.is_confirmed = true";
        return jdbc.query(GET_FRIENDS_QUERY, mapper, userId);
    }

    @Override
    public Collection<User> getCommonFriends(Long userId, Long otherUserId) {
        return jdbc.query(GET_COMMON_FRIENDS_QUERY, mapper, userId, otherUserId);
    }
}
