package ru.practicum.service;

import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;

import java.time.LocalDateTime;
import java.util.List;

public interface CommentService {
    CommentDto addComment(NewCommentDto newCommentDto, Long userId, Long eventId);

    CommentDto updateCommentByAuthor(NewCommentDto newCommentDto, Long userId, Long commentId);

    CommentDto updateCommentByAdmin(NewCommentDto newCommentDto, Long commentId);

    CommentDto getByCommentId(Long userId, Long commentId);

    List<CommentDto> getAllByCreateTime(Long userId, LocalDateTime start, LocalDateTime end, Integer from, Integer size);

    void deleteCommentByAuthor(Long userId, Long commentId);

    List<CommentDto> getCommentsByEventIdByAdmin(Long eventId, Integer from, Integer size);

    CommentDto getByCommentIdByAdmin(Long commentId);

    void deleteCommentByAdmin(Long commentId);
}