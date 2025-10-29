package ru.practicum.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.event.validator.ValidEventDate;
import ru.practicum.helper.RequestParamHelper;
import ru.practicum.location.dto.NewLocationRequest;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewEventRequest {
    @NotBlank
    @Size(min = 3, max = 120)
    String title;

    @NotBlank
    @Size(min = 20, max = 2000)
    String annotation;

    @NotBlank
    @Size(min = 20, max = 7000)
    String description;

    @NotNull
    Long category;

    @NotNull
    @ValidEventDate
    @JsonFormat(pattern = RequestParamHelper.DATE_TIME_FORMAT)
    LocalDateTime eventDate;

    @NotNull
    NewLocationRequest location;

    Boolean paid = false;

    @PositiveOrZero
    Integer participantLimit;

    Boolean requestModeration = true;

    //метод проверки наличия поля, иначе ставим 0
    public boolean hasParticipantLimit() {
        return participantLimit != null;
    }

}
