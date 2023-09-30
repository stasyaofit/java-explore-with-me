package ru.practicum.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.service.CommentService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;


@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
@Slf4j
public class AdminCommentController {
    private final CommentService commentService;

    @GetMapping
    public List<CommentDto> getCommentsByEventId(@RequestParam("eventId") Long eventId,
                                         @PositiveOrZero @RequestParam(value = "from", defaultValue = "0") Integer from,
                                         @Positive @RequestParam(value = "size", defaultValue = "10") Integer size) {
        log.info("������� GET-������ � ���������: /admin/comments/:eventId �� ��������� ������ ������������ �������" +
                "� id = {}  ���������������.", eventId);

        return commentService.getCommentsByEventIdByAdmin(eventId, from, size);
    }

    @GetMapping("/{commentId}")
    public CommentDto getByCommentId(@PathVariable("commentId") Long commentId) {
        log.info("������� GET-������ � ���������: /admin/comments//{commentId} �� ��������� ����������� � id = {} " +
                " ���������������.", commentId);
        return commentService.getByCommentIdByAdmin(commentId);
    }

    @PatchMapping("/{commentId}")
    public CommentDto updateCommentByAdmin(@RequestBody @Valid NewCommentDto newCommentDto,
                             @PathVariable("commentId") Long commentId) {
        log.info("������� PATCH-������ � ���������: /admin/comments//{commentId} �� ���������� ����������� � id = {} " +
                " ���������������.", commentId);
        return commentService.updateCommentByAdmin(newCommentDto, commentId);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCommentByAdmin(@PathVariable("commentId") Long commentId) {
        log.info("������� DELETE-������ � ���������: /admin/comments//{commentId} �� �������� ����������� � id = {} " +
                " ���������������.", commentId);
        commentService.deleteCommentByAdmin(commentId);
    }
}