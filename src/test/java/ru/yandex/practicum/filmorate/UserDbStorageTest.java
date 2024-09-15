package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dao.UserDbStorage;
import ru.yandex.practicum.filmorate.dao.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, UserRowMapper.class})
class UserDbStorageTest {
    private final UserDbStorage userStorage;

    @Test
    public void testAddUser() {
        User newUser = new User();
        newUser.setEmail("newuser@example.com");
        newUser.setLogin("newlogin");
        newUser.setName("New User");
        newUser.setBirthday(LocalDate.of(1990, 1, 1));

        User addedUser = userStorage.addUser(newUser);

        assertThat(addedUser).isNotNull();
        assertThat(addedUser.getId()).isNotNull();
        assertThat(addedUser.getEmail()).isEqualTo("newuser@example.com");
    }

    @Test
    public void testUpdateUser() {
        User user = userStorage.getUserById(1L);
        user.setName("Updated User");

        User updatedUser = userStorage.updateUser(user);

        assertThat(updatedUser.getName()).isEqualTo("Updated User");
    }

    @Test
    public void testGetAllUsers() {
        Collection<User> users = userStorage.getAllUsers();

        assertThat(users).hasSize(3);
    }

    @Test
    public void testFindUserById() {
        Optional<User> userOptional = Optional.ofNullable(userStorage.getUserById(1L));

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", 1L)
                );
    }

    @Test
    public void testAddFriend() {
        userStorage.addFriend(1L, 2L);  // User 1 adds User 2 as friend

        Collection<User> friends = userStorage.getFriends(1L);
        assertThat(friends).hasSize(1);
    }

    @Test
    public void testGetFriends() {
        userStorage.addFriend(1L, 2L);
        Collection<User> friends = userStorage.getFriends(1L);

        assertThat(friends).hasSize(1);
    }

    @Test
    public void testRemoveFriend() {
        userStorage.removeFriend(1L, 2L);  // User 1 removes User 2 as friend

        Collection<User> friends = userStorage.getFriends(1L);
        assertThat(friends).isEmpty();
    }

    @Test
    public void testGetCommonFriends() {
        Collection<User> commonFriends = userStorage.getCommonFriends(1L, 2L);

        assertThat(commonFriends).isEmpty();  // Assuming no common friends at the start
    }
}
