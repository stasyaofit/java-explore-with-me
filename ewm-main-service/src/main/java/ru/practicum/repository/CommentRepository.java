package ru.practicum.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.model.Comment;

import java.time.LocalDateTime;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByEvent_Id(Long eventId, Pageable pageable);

    @Query("select c from Comment c where c.author.id = ?1 and c.created > ?2 order by c.created")
    List<Comment> findCommentsByAuthorAndTimeAfter(Long userId, LocalDateTime start, Pageable pageable);

    @Query("select c from Comment c where c.author.id = ?1 and c.created < ?2 order by c.created")
    List<Comment> findCommentsByAuthorAndTimeBefore(Long userId, LocalDateTime end, Pageable pageable);

    @Query("select c from Comment c " +
            "where c.author.id = ?1 and c.created > ?2 and c.created < ?3 " +
            "order by c.created")
    List<Comment> findCommentsByAuthorAndTimeBetween(Long userId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<Comment> findAllByAuthor_Id(Long userId, Pageable pageable);
}
