package ru.practicum.shareit.integration;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
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
    long requestorId, anotherUserId, ownerId;
    long item1Id, item2Id;
    private User requestor, anotherUser, owner;
    private RequestDesc dto, dto1, dto2, dto3;
    private RequestWithItems ansDto;
    private ItemDto item1Dto, item2Dto;

    @BeforeEach
    public void createEntities() {
        requestor = new User(null, "requestor", "requestor@email.ru");
        anotherUser = new User(null, "another_requestor", "another_requestor@email.ru");
        requestorId = userService.createUser(requestor).getId();
        anotherUserId = userService.createUser(anotherUser).getId();
        owner = new User(null, "owner", "owner@email.ru");
        ownerId = userService.createUser(owner).getId();

        dto1 = new RequestDesc("request_desc1");
        dto2 = new RequestDesc("request_desc2");
        dto3 = new RequestDesc("request_desc3");

        requestorId = userService.createUser(requestor).getId();
        dto = new RequestDesc("request_desc");

        item1Dto = ItemDto.builder().name("item1_name").description("item1_desc").available(true).requestId(null).build();
        item2Dto = ItemDto.builder().name("item2_name").description("item2_desc").available(true).requestId(null).build();

    }

    @Test
    @Transactional
    public void createRequest_succeed() {
        assertThat(ansDto = requestService.createRequest(dto, requestorId))
                .hasFieldOrPropertyWithValue("id", ansDto.getId())
                .hasFieldOrPropertyWithValue("description", dto.getDescription())
                .hasFieldOrProperty("created")
                .hasFieldOrProperty("items");
    }

    @Test
    @Transactional
    public void findAllOwnersRequests_succeed() {
        requestService.createRequest(dto1, requestorId);
        requestService.createRequest(dto2, requestorId);
        requestService.createRequest(dto3, anotherUserId);

        assertThat(requestService.findAllOwnersRequests(requestorId)).hasSize(2);
    }

    @Test
    @Transactional
    public void findAllRequests_succeed() {
        requestService.createRequest(dto1, requestorId);
        requestService.createRequest(dto2, requestorId);
        requestService.createRequest(dto3, anotherUserId);

        assertThat(requestService.findAllRequests(anotherUserId, 0, 20)).hasSize(2);
    }

    @Test
    @Transactional
    public void findRequestById_succeed() {
        RequestWithItems ansDto = requestService.createRequest(dto1, requestorId);
        long requestId = ansDto.getId();

        item1Dto.setRequestId(requestId);
        item2Dto.setRequestId(requestId);
        item1Id = itemService.createItem(item1Dto, ownerId).getId();
        item2Id = itemService.createItem(item2Dto, ownerId).getId();

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
