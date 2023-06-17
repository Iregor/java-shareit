package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.exceptions.EntityNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.RequestDesc;
import ru.practicum.shareit.request.dto.RequestWithItems;
import ru.practicum.shareit.request.mapper.RequestMapper;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.request.repository.RequestRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public RequestWithItems createRequest(RequestDesc requestDesc, long userId) {
        Request request = Request.builder()
                .description(requestDesc.getDescription())
                .requestor(userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found.", userId, String.valueOf(Thread.currentThread().getStackTrace()[1]))))
                .created(LocalDateTime.now())
                .build();
        return RequestMapper.toRequestWithItems(requestRepository.save(request));
    }

    @Override
    public List<RequestWithItems> findAllOwnersRequests(long userId) {
        assertUserExists(userId);
        return requestRepository.findAllByRequestorIdOrderByCreatedDesc(userId)
                .stream()
                .map(request -> RequestMapper.toRequestWithItems(request, findItemsForRequest(request)))
                .collect(Collectors.toList());
    }

    @Override
    public List<RequestWithItems> findAllRequests(long userId, int from, int size) {
        assertUserExists(userId);
        //Закомментирована реализация, которая соответствует требованиям ТЗ (отображает список элементов с ТРЕБУЕМОГО индекса)
        //Оставленная незакомментированной реализация выводит страницу, на которой расположен элемент с требуемым индексом, т.к.
        //судя по всему имелось в виду именно это, хотя формулировка ТЗ указывает на другое.

/*        Pageable fromPage = PageRequest.of(from / size, size, Sort.by("created").descending());
        Pageable extraPage = PageRequest.of(from / size + 1, size, Sort.by("created").descending());
        Stream<Request> streamFromPage = requestRepository.findAllByRequestorIdNot(userId, fromPage).stream();
        Stream<Request> streamExtraPage = from % size != 0 ?
                requestRepository.findAllByRequestorIdNot(userId, extraPage).stream()
                : Stream.empty();
        return Stream.concat(streamFromPage, streamExtraPage)
                .skip(from % size)
                .limit(size)
                .map(request -> RequestMapper.toRequestWithItems(request, findItemsForRequest(request)))
                .collect(Collectors.toList());*/
        Pageable page = PageRequest.of(from / size, size, Sort.by("created").descending());
        return requestRepository.findAllByRequestorIdNot(userId, page)
                .stream()
                .map(request -> RequestMapper.toRequestWithItems(request, findItemsForRequest(request)))
                .collect(Collectors.toList());
    }

    @Override
    public RequestWithItems findRequestById(long userId, long requestId) {
        assertUserExists(userId);
        Request request = requestRepository.findById(requestId).orElseThrow(() -> new EntityNotFoundException("Request not found.", requestId, String.valueOf(Thread.currentThread().getStackTrace()[1])));
        return RequestMapper.toRequestWithItems(request, findItemsForRequest(request));
    }

    private void assertUserExists(long userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found.", userId, String.valueOf(Thread.currentThread().getStackTrace()[1]));
        }
    }

    private List<Item> findItemsForRequest(Request request) {
        return itemRepository.findAllByItemRequestId(request.getId());
    }
}
