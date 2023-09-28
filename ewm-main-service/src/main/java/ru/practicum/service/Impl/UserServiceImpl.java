package ru.practicum.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.user.NewUserRequestDto;
import ru.practicum.dto.user.UserDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.UserMapper;
import ru.practicum.model.User;
import ru.practicum.repository.UserRepository;
import ru.practicum.service.UserService;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository repository;
    private final UserMapper mapper;

    @Override
    @Transactional
    public UserDto addUser(NewUserRequestDto userRequestDto) {
        repository.findFirst1ByName(userRequestDto.getName()).ifPresent((user) -> {
            throw new ConflictException("Пользователь уже существует.");
        });
        User newUser = mapper.mapToUser(userRequestDto);
        User savedUser = repository.save(newUser);
        log.info("Пользователь " + savedUser + " успешно добавлен.");
        return mapper.mapToUserDto(savedUser);
    }

    @Override
    public List<UserDto> getAll(@Nullable List<Long> ids, Integer from, Integer size) {
        if (Objects.nonNull(ids)) {
            List<User> users = repository.findAllById(ids);
            return users.stream()
                    .map(mapper::mapToUserDto)
                    .collect(Collectors.toList());
        } else {
            int page = from / size;
            Page<User> users = repository.findAll(PageRequest.of(page, size));
            return users.map(mapper::mapToUserDto).getContent();
        }
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден."));
        repository.delete(user);
        log.info("Пользователь с id = " + userId + " успешно удален.");
    }
}
