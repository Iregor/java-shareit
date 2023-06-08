package ru.practicum.shareit.integration;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserServiceImpl;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional
public class UserServiceImplTest {
    private final UserServiceImpl userService;

    @Test
    public void createUser_validUser_succeed() {
        User user = UserMapper.toUser(userService.createUser(new User(null, "name", "email@mail.ru")));
        assertThat(userService.findUserById(user.getId())).isEqualTo(UserMapper.toDto(user));
    }

    @Test
    public void findAllUsers_2users_succeed() {
        User user1, user2;
        userService.createUser(user1 = new User(null, "name1", "email1@mail.ru"));
        userService.createUser(user2 = new User(null, "name2", "email2@mail.ru"));

        assertThat(userService.findAllUsers()).hasSize(2)
                .element(0).isEqualTo(UserMapper.toDto(user1));
        assertThat(userService.findAllUsers()).hasSize(2)
                .element(1).isEqualTo(UserMapper.toDto(user2));
    }

    @Test
    public void findAllUsers_0users_succeed() {
        assertThat(userService.findAllUsers()).hasSize(0);
    }

    @Test
    public void patchUser_succeed() throws IllegalAccessException {
        User user = UserMapper.toUser(userService.createUser(new User(null, "name1", "email1@mail.ru")));
        userService.patchUser(new User(null, "updatedName", null), user.getId());
        user.setName("updatedName");
        assertThat(userService.findUserById(user.getId())).isEqualTo(UserMapper.toDto(user));
    }

    @Test
    public void deleteUserById_succeed() {
        User user = UserMapper.toUser(userService.createUser(new User(null, "name1", "email1@mail.ru")));
        assertThat(userService.findAllUsers()).hasSize(1);
        userService.deleteUserById(user.getId());
        assertThat(userService.findAllUsers()).hasSize(0);
    }
}
