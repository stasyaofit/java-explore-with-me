package ru.practicum.controller.priv;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.service.CommentService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.util.DateTimeFormatter.DATE_TIME_FORMAT;

@RestController
@RequestMapping("/users/{userId}/comments")
@Validated
@RequiredArgsConstructor
@Slf4j
public class PrivateCommentController {
    private final CommentService commentService;

    @PostMapping("/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto addComment(@RequestBody @Valid NewCommentDto newCommentDto,
                          @PathVariable("userId") Long userId,
                          @PathVariable("eventId") Long eventId) {
        log.info("������� POST-������ � ���������: /users/{userId}/comments/{eventId} �� ���������� ����������� " +
                "� ������� � id = {} �� ������������ � id = {}.", eventId, userId);
        return commentService.addComment(newCommentDto, userId, eventId);
    }

    @PatchMapping("/{commentId}")
    public CommentDto updateByAuthorId(@RequestBody @Valid NewCommentDto newCommentDto,
                                       @PathVariable("userId") Long userId,
                                       @PathVariable("commentId") Long commentId) {
        log.info("������� PATCH-������ � ���������: /users/{userId}/comments/{commentId} �� ���������� ����������� � id = {} " +
                " �� ������������ � id = {}.", commentId, userId);
        return commentService.updateCommentByAuthor(newCommentDto, userId, commentId);
    }

    @GetMapping("/{commentId}")
    public CommentDto getByCommentId(@PathVariable("userId") Long userId,
                                     @PathVariable("commentId") Long commentId) {
        log.info("������� GET-������ � ���������: /users/{userId}/comments/{commentId} �� ��������� ����������� � id = {} " +
                " �� ������������ � id = {}.", commentId, userId);
        return commentService.getByCommentId(userId, commentId);
    }

    @GetMapping
    public List<CommentDto> getAllByCreateTime(@PathVariable(value = "userId") Long userId,
                                               @PositiveOrZero @RequestParam(value = "from", defaultValue = "0") Integer from,
                                               @Positive @RequestParam(value = "size", defaultValue = "10") Integer size,
                                               @RequestParam(value = "start", required = false)
                                               @DateTimeFormat(pattern = DATE_TIME_FORMAT)
                                               LocalDateTime start,
                                               @RequestParam(value = "end", required = false)
                                               @DateTimeFormat(pattern = DATE_TIME_FORMAT)
                                               LocalDateTime end) {
        log.info("������� GET-������ � ���������: /users/{userId}/comments �� ��������� ������ ������������ " +
                "������������� � id = {}.", userId);
        return commentService.getAllByCreateTime(userId, start, end, from, size);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteByAuthorId(@PathVariable("userId") Long userId,
                                 @PathVariable("commentId") Long commentId) {
        log.info("������� DELETE-������ � ���������: /users/{userId}/comments/{commentId} �� �������� ����������� � id = {} " +
                " �� ������������ � id = {}.", commentId, userId);
        commentService.deleteCommentByAuthor(userId, commentId);
    }
}
