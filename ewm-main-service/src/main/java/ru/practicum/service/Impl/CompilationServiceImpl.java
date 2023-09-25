package ru.practicum.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CompilationMapper;
import ru.practicum.model.Compilation;
import ru.practicum.model.Event;
import ru.practicum.repository.CompilationRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.service.CompilationService;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepo;
    private final EventRepository eventRepo;
    private final CompilationMapper compilationMapper;

    @Override
    @Transactional
    public CompilationDto addCompilation(NewCompilationDto newCompDto) {
        Set<Event> events = fetchEvents(newCompDto.getEvents());
        Compilation newComp = compilationMapper.mapToCompilation(newCompDto, events);
        Compilation savedComp = compilationRepo.save(newComp);
        log.info("Подборка событий " + savedComp.getTitle() + " успешно сохранена.");
        return compilationMapper.mapToCompilationDto(savedComp);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest request) {
        Compilation compilation = checkCompilationExistAndGet(compId);
        Set<Long> eventsIds = request.getEvents();
        Set<Event> updatedEvents = fetchEvents(eventsIds);
        compilation.setEvents(updatedEvents);
        if (Objects.nonNull(request.getPinned())) {
            compilation.setPinned(request.getPinned());
        }
        String title = request.getTitle();
        if (title != null && title.isBlank()) {
            if (compilationRepo.existsByTitleAndIdNot(title, compilation.getId())) {
                throw new ConflictException("Подборка событий уже существует, название " + title + " не может быть использовано.");
            }
            compilation.setTitle(title);
        }
        Compilation updatedComp = compilationRepo.save(compilation);
        log.info("Подборка событий " + updatedComp + " успешно обновлена.");
        return compilationMapper.mapToCompilationDto(updatedComp);
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        Compilation compilation = checkCompilationExistAndGet(compId);
        compilationRepo.delete(compilation);
        log.info("Подборка событий с id = " + compId + " успешно удалена.");
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> getAll(Boolean pinned, Integer from, Integer size) {
        Page<Compilation> compilations;
        if (pinned == null) {
            compilations = compilationRepo.findAll(PageRequest.of(from / size, size));
        } else {
            compilations = compilationRepo.findAllByPinned(pinned, PageRequest.of(from / size, size));
        }
        return compilations.map(compilationMapper::mapToCompilationDto).getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = checkCompilationExistAndGet(compId);
        return compilationMapper.mapToCompilationDto(compilation);
    }

    private Compilation checkCompilationExistAndGet(Long comId) {
        return compilationRepo.findById(comId)
                .orElseThrow(() -> new NotFoundException("Подборка событий с id = " + comId + " не найдена."));
    }

    private Set<Event> fetchEvents(Set<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.emptySet();
        }
        return new HashSet<>(eventRepo.findAllById(eventIds));
    }
}