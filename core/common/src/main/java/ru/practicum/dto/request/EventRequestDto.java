package ru.practicum.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import ru.practicum.helper.RequestParamHelper;

import java.time.LocalDateTime;

@Data
@Builder
public class EventRequestDto {
    private Long id;
    private Long event;
    private Long requester;
    private Status status;

    @JsonFormat(pattern = RequestParamHelper.DATE_TIME_FORMAT)
    private LocalDateTime created;
}
