package ru.practicum.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.model.Category;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.service.CategoryService;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryDto addCategory(NewCategoryDto category) {
        Category newCategory = categoryMapper.mapToCategory(category);
        Category savedCategory = categoryRepository.save(newCategory);
        return categoryMapper.mapToCategoryDto(savedCategory);
    }

    @Override
    public void deleteCategory(Long catId) {
        Category category = checkCategoryExistAndGet(catId);
        if (eventRepository.findByCategoryId(catId).isPresent()) {
            throw new ConflictException("Категория связана с событиями и не может быть удалена.");
        }
        categoryRepository.delete(category);
        log.info("Категория с id = " + catId + " успешно удалена.");
    }

    @Override
    public CategoryDto updateCategory(CategoryDto categoryDto, Long catId) {
        Category category = checkCategoryExistAndGet(catId);
        String newName = categoryDto.getName();
        String existingName = category.getName();
        category.setName(StringUtils.defaultIfBlank(newName, existingName));
        log.info("Категория " + category + " обновлена.");
        return categoryMapper.mapToCategoryDto(categoryRepository.save(category));
    }

    @Override
    public List<CategoryDto> getAll(Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from, size);
        Page<Category> catPage = categoryRepository.findAll(pageable);
        return catPage.map(categoryMapper::mapToCategoryDto).getContent();
    }

    @Override
    public CategoryDto getCategoryById(Long catId) {
        Category category = checkCategoryExistAndGet(catId);
        return categoryMapper.mapToCategoryDto(category);
    }

    private Category checkCategoryExistAndGet(Long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Категория с id = " + catId + " не найдена."));
    }
}