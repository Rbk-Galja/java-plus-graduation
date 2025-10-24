package ru.practicum.eventRequest.dto;

import lombok.*;
import ru.practicum.dto.request.EventRequestDto;

import java.util.List;

@Builder
@Data
public class EventRequestUpdateResult {
    List<EventRequestDto> confirmedRequests;
    List<EventRequestDto> rejectedRequests;
}

