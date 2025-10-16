package ru.practicum.compilation.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UpdateCompilationRequest {
    List<Long> events;
    Boolean pinned;

    @Size(max = 50, message = "Заголовок не должен превышать 50 символов")
    String title;
}
