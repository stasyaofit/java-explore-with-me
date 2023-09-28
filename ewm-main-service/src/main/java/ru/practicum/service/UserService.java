package ru.practicum.service;

import ru.practicum.dto.user.NewUserRequestDto;
import ru.practicum.dto.user.UserDto;

import java.util.List;

public interface UserService {

    List<UserDto> getAll(List<Long> ids, Integer from, Integer size);

    UserDto addUser(NewUserRequestDto request);

    void deleteUser(Long userId);
}