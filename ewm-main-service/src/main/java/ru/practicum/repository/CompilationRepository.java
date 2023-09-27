package ru.practicum.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.model.Compilation;

import java.util.List;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {
    @Query("SELECT c FROM Compilation c WHERE :pinned is null or c.pinned = :pinned")
    List<Compilation> findAllByPinned(Boolean pinned, Pageable pageable);

    Boolean existsByTitleAndIdNot(String newTitle, Long compId);
}
