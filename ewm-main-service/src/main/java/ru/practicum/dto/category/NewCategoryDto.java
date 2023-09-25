package ru.practicum.dto.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NewCategoryDto {
    @NotBlank(message = "Название категории не может быть пустым")
    @Length(min = 1, max = 50, message = "Название категории должно быть в пределах от 1 до 50 символов")
    private String name;
}