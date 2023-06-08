package ru.practicum.shareit.integration;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.dto.RequestDesc;
import ru.practicum.shareit.request.dto.RequestWithItems;
import ru.practicum.shareit.request.service.RequestServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserServiceImpl;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Transactional
public class RequestServiceImplTest {
    private final ItemServiceImpl itemService;
    private final UserServiceImpl userService;
    private final RequestServiceImpl requestService;

    @Test
    @Transactional
    public void createRequest_succeed() {
        User requestor = new User(null, "requestor", "requestor@email.ru");
        long requestorId = userService.createUser(requestor).getId();

        RequestDesc dto = new RequestDesc("request_desc");

        RequestWithItems ansDto;
        assertThat(ansDto = requestService.createRequest(dto, requestorId))
                .hasFieldOrPropertyWithValue("id", ansDto.getId())
                .hasFieldOrPropertyWithValue("description", dto.getDescription())
                .hasFieldOrProperty("created")
                .hasFieldOrProperty("items");
    }

    @Test
    @Transactional
    public void findAllOwnersRequests_succeed() {
        User requestor = new User(null, "requestor", "requestor@email.ru");
        long requestorId = userService.createUser(requestor).getId();
        User anotherUser = new User(null, "another_requestor", "another_requestor@email.ru");
        long anotherUserId = userService.createUser(anotherUser).getId();

        RequestDesc dto1 = new RequestDesc("request_desc1");
        RequestDesc dto2 = new RequestDesc("request_desc2");
        RequestDesc dto3 = new RequestDesc("request_desc3");

        requestService.createRequest(dto1, requestorId);
        requestService.createRequest(dto2, requestorId);
        requestService.createRequest(dto3, anotherUserId);

        assertThat(requestService.findAllOwnersRequests(requestorId)).hasSize(2);
    }

    @Test
    @Transactional
    public void findAllRequests_succeed() {
        User requestor = new User(null, "requestor", "requestor@email.ru");
        long requestorId = userService.createUser(requestor).getId();
        User anotherUser = new User(null, "another_requestor", "another_requestor@email.ru");
        long anotherUserId = userService.createUser(anotherUser).getId();

        RequestDesc dto1 = new RequestDesc("request_desc1");
        RequestDesc dto2 = new RequestDesc("request_desc2");
        RequestDesc dto3 = new RequestDesc("request_desc3");

        requestService.createRequest(dto1, requestorId);
        requestService.createRequest(dto2, requestorId);
        requestService.createRequest(dto3, anotherUserId);

        assertThat(requestService.findAllRequests(anotherUserId, 0, 20)).hasSize(2);
    }

    @Test
    @Transactional
    public void findRequestById_succeed() {
        User requestor = new User(null, "requestor", "requestor@email.ru");
        long requestorId = userService.createUser(requestor).getId();
        User owner = new User(null, "owner", "owner@email.ru");
        long ownerId = userService.createUser(owner).getId();


        RequestDesc dto1 = new RequestDesc("request_desc1");
        RequestWithItems ansDto = requestService.createRequest(dto1, requestorId);
        long requestId = ansDto.getId();

        ItemDto itemDto1 = ItemDto.builder().name("item1_name").description("item1_desc").available(true).requestId(requestId).build();
        ItemDto itemDto2 = ItemDto.builder().name("item2_name").description("item2_desc").available(true).requestId(requestId).build();
        long item1Id = itemService.createItem(itemDto1, ownerId).getId();
        long item2Id = itemService.createItem(itemDto2, ownerId).getId();

        assertThat(ansDto = requestService.findRequestById(requestorId, requestId))
                .hasFieldOrPropertyWithValue("id", requestId)
                .hasFieldOrPropertyWithValue("description", ansDto.getDescription());

        assertThat(ansDto.getItems())
                .hasSize(2)
                .element(0).hasFieldOrPropertyWithValue("id", item1Id);
        assertThat(ansDto.getItems())
                .element(1).hasFieldOrPropertyWithValue("id", item2Id);
    }
}
