package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserTests {

    private final Validator validator;

    public UserTests() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testValidUser() {
        User user = new User();
        user.setEmail("random-email@yandex.ru");
        user.setLogin("randomLogin");
        user.setName("Name Surname");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(0, violations.size());
    }

    @Test
    public void testInvalidEmail() {
        User user = new User();
        user.setEmail("random-email");
        user.setLogin("randomLogin");
        user.setName("Name Surname");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(1, violations.size());
        assertEquals("Некорректная электронная почта", violations.iterator().next().getMessage());
    }

    @Test
    public void testEmptyLogin() {
        User user = new User();
        user.setEmail("random-email@yandex.ru");
        user.setLogin(null);
        user.setName("Name Surname");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(1, violations.size());
        assertEquals("Логин не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    public void testLoginWithSpaces() {
        User user = new User();
        user.setEmail("random-email@yandex.ru");
        user.setLogin("Incorrect Login");
        user.setName("Name Surname");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(1, violations.size());
        assertEquals("Логин не может содержать пробелы", violations.iterator().next().getMessage());
    }

    @Test
    public void testFutureBirthday() {
        User user = new User();
        user.setEmail("random-email@yandex.ru");
        user.setLogin("randomLogin");
        user.setName("Name Surname");
        user.setBirthday(LocalDate.of(2999, 1, 1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(1, violations.size());
        assertEquals("Дата рождения не может быть в будущем", violations.iterator().next().getMessage());
    }
}
