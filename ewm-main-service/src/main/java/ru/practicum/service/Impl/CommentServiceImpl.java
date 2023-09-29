package ru.practicum.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CommentMapper;
import ru.practicum.model.Comment;
import ru.practicum.model.Event;
import ru.practicum.model.User;
import ru.practicum.repository.CommentRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.UserRepository;
import ru.practicum.service.CommentService;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CommentRepository commentsRepository;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public CommentDto addComment(NewCommentDto newCommentDto, Long userId, Long eventId) {
        User user = checkUserExistAndGet(userId);
        Event event = checkEventExistAndGet(eventId);
        Comment comment = new Comment();
        comment.setAuthor(user);
        comment.setEvent(event);
        comment.setCreated(LocalDateTime.now());
        comment.setText(newCommentDto.getText());
        return commentMapper.mapToCommentDto(commentsRepository.save(comment));
    }

    @Override
    @Transactional
    public CommentDto updateCommentByAuthor(NewCommentDto newCommentDto, Long userId, Long commentId) {
        Comment oldComment = checkCommentExistAndGet(commentId);
        checkUserExistAndGet(userId);
        if (!oldComment.getAuthor().getId().equals(userId)) {
            throw new ConflictException("Невозможно обновить комментарий, т.к. пользователь не является его автором.");
        }
        oldComment.setText(newCommentDto.getText());
        Comment savedComment = commentsRepository.save(oldComment);
        log.info("Комментарий с id = {} обновлён.", commentId);
        return commentMapper.mapToCommentDto(savedComment);
    }

    @Override
    @Transactional
    public CommentDto updateCommentByAdmin(NewCommentDto newCommentDto, Long commentId) {
        Comment oldComment = checkCommentExistAndGet(commentId);
        oldComment.setText(newCommentDto.getText());
        Comment savedComment = commentsRepository.save(oldComment);
        log.info("Комментрарий с id = {} обновлён.", commentId);
        return commentMapper.mapToCommentDto(savedComment);
    }

    @Override
    public CommentDto getByCommentId(Long userId, Long commentId) {
        Comment comment = checkCommentExistAndGet(commentId);
        checkUserExistAndGet(userId);
        if (!userId.equals(comment.getAuthor().getId())) {
            throw new ConflictException("Невозможно получить комментарий, т.к. пользователь не является его автором.");
        }
        return commentMapper.mapToCommentDto(comment);
    }

    @Override
    public List<CommentDto> getAllByCreateTime(Long userId, LocalDateTime createStart, LocalDateTime createEnd, Integer from, Integer size) {
        checkUserExistAndGet(userId);
        List<Comment> comments;
        Pageable page = PageRequest.of((int) from / size, size);
        if (createStart != null && createEnd != null) {
            if (createEnd.isBefore(createStart)) {
                throw new ConflictException("createEnd должен быть после createStart");
            } else {
                comments = commentsRepository.findCommentsByAuthorAndTimeBetween(
                        userId, createStart, createEnd, page);
            }
        } else if (createStart != null) {
            comments = commentsRepository.findCommentsByAuthorAndTimeAfter(userId, createStart, page);
        } else if (createEnd != null) {
            comments = commentsRepository.findCommentsByAuthorAndTimeBefore(userId, createEnd, page);
        } else {
            comments = commentsRepository.findAllByAuthor_Id(userId, page);
        }
        return commentMapper.mapToCommentDtoList(comments);
    }

    @Override
    @Transactional
    public void deleteCommentByAuthor(Long userId, Long commentId) {
        Comment comment = checkCommentExistAndGet(commentId);
        checkUserExistAndGet(userId);
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ConflictException("Невозможно удалить комментарий, т.к. пользователь не является его автором.");
        }
        log.info("Комментрарий с id = {} обновлён.", commentId);
        commentsRepository.delete(comment);
    }

    @Override
    public List<CommentDto> getCommentsByEventIdByAdmin(Long eventId, Integer from, Integer size) {
        checkEventExistAndGet(eventId);
        Pageable page = PageRequest.of(from / size, size);
        List<Comment> eventComments = commentsRepository.findAllByEvent_Id(eventId, page);
        return commentMapper.mapToCommentDtoList(eventComments);
    }

    @Override
    public CommentDto getByCommentIdByAdmin(Long commentId) {
        Comment comment = checkCommentExistAndGet(commentId);
        return commentMapper.mapToCommentDto(comment);
    }

    @Override
    @Transactional
    public void deleteCommentByAdmin(Long commentId) {
        commentsRepository.deleteById(commentId);
        log.debug("Комментарий с id = {} удалён администратором.", commentId);
    }

    private User checkUserExistAndGet(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id = " + userId + " не найден."));
    }

    private Comment checkCommentExistAndGet(Long commentId) {
        return commentsRepository.findById(commentId).orElseThrow(() ->
                new NotFoundException("Комментарий с id = " + commentId + " не найден."));
    }

    private Event checkEventExistAndGet(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() ->
                new NotFoundException("Событие с id = " + eventId + " не найдено."));
    }
}