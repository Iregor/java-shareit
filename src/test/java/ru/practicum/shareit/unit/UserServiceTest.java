package ru.practicum.shareit.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exception.exceptions.EntityNotFoundException;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserServiceImpl;

import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.lang.reflect.Field;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
public class UserServiceTest {
    @Autowired
    Validator validator;
    @InjectMocks
    UserServiceImpl userService;
    @Mock
    UserRepository userRepository;
    User user1, user2, user3;
    HashMap<Long, User> users;

    @BeforeEach
    public void prepareTestEnvironment() throws IllegalAccessException {
        createEntities();
    }

    public void createEntities() throws IllegalAccessException {
        users = new HashMap<>();
        user1 = new User(1L, "user1", "email1@yandex.ru");
        user2 = new User(2L, "user2", "email2@yandex.ru");
        user3 = new User(3L, "user3", "email3@yandex.ru");
        users.put(user1.getId(), user1);
        users.put(user2.getId(), user2);
        users.put(user3.getId(), user3);

        //инъекция валидатора из контекста приложения для встроенной логики валидации
        try {
            Field field = userService.getClass().getDeclaredField("validator");
            field.setAccessible(true);
            field.set(userService, validator);
        } catch (NoSuchFieldException exc) {
            System.out.println("Error during accessing field validator");
        }
    }

    public void setMocksBehaviour() {
        MockBehaviourManager.setUserRepositoryBehaviour(userRepository, users);
    }

    /*UserDto createUser(User user) - создает пользователя
     * Присваивает новому пользователю id*/
    @Test
    public void createUserTest_validIncome_succeed() {
        setMocksBehaviour();
        User user4 = new User(null, "user4_name", "user4@email.ru");
        assertThat(userService.createUser(user4))
                .hasFieldOrPropertyWithValue("id", 4L)
                .hasFieldOrPropertyWithValue("name", "user4_name")
                .hasFieldOrPropertyWithValue("email", "user4@email.ru");
    }

    /*UserDto findUserById(Long userId) - находит пользователя по id*/

    @Test
    public void findUserByIdTest_validId_succeed() {
        setMocksBehaviour();
        assertThat(userService.findUserById(user1.getId())).isEqualTo(UserMapper.toDto(user1));
    }

    @Test
    public void findUserByIdTest_notExistingId_exceptionThrown() {
        setMocksBehaviour();
        long notExistingId = 100L;
        assertThatThrownBy(() -> userService.findUserById(notExistingId)).isInstanceOf(EntityNotFoundException.class);
    }

    /*List<UserDto> findAllUsers() - нахождение всех пользователей*/
    @Test
    public void findAllUsersTest_notEmptyList_succeed() {
        setMocksBehaviour();
        assertThat(userService.findAllUsers()).hasSize(3)
                .contains(UserMapper.toDto(user1))
                .contains(UserMapper.toDto(user2))
                .contains(UserMapper.toDto(user3));

    }

    @Test
    public void findAllUsersTest_emptyList_succeed() {
        users.clear();
        setMocksBehaviour();
        assertThat(userService.findAllUsers()).hasSize(0);
    }

    /*UserDto patchUser(User user) - обновляет поля пользователя*/

    @Test
    public void patchUserTest_updatedName_succeed() throws IllegalAccessException {
        setMocksBehaviour();

        user1.setName("updatedName");
        assertThat(userService.patchUser(new User(null, "updatedName", null), user1.getId())).isEqualTo(UserMapper.toDto(user1));
    }

    @Test
    public void patchUserTest_updatedEmail_succeed() throws IllegalAccessException {
        setMocksBehaviour();


        user1.setEmail("updated@email.ru");
        assertThat(userService.patchUser(new User(null, null, "updated@email.ru"), user1.getId())).isEqualTo(UserMapper.toDto(user1));
    }

    @Test
    public void patchUserTest_notExistingUser_exceptionThrown() {
        setMocksBehaviour();

        long notExistingUserid = 100L;

        assertThatThrownBy(() -> userService.patchUser(new User(null, "updatedName", null), notExistingUserid))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void patchUserTest_notValidName_exceptionThrown() {
        setMocksBehaviour();

        String invalidName = "  ";

        assertThatThrownBy(() -> userService.patchUser(new User(null, invalidName, null), user1.getId()))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    public void patchUserTest_notValidEmail_exceptionThrown() {
        setMocksBehaviour();

        String notValidEmail = "updatedEmail.ru";

        assertThatThrownBy(() -> userService.patchUser(new User(null, null, notValidEmail), user1.getId()))
                .isInstanceOf(ConstraintViolationException.class);
    }
}
