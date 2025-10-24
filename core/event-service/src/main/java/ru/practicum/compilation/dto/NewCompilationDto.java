package ru.practicum.compilation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewCompilationDto {
    List<Long> events;
    Boolean pinned = false;

    @NotNull(message = "Необходимо указать заголовок")
    @NotBlank(message = "Необходимо указать заголовок")
    @Size(max = 50, message = "Заголовок не должен превышать 50 символов")
    String title;
}