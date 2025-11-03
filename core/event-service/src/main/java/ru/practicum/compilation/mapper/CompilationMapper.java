package ru.practicum.compilation.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.client.AnalyzerClient;
import ru.practicum.compilation.dto.CompilationDto;
import ru.practicum.compilation.dto.NewCompilationDto;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.dto.request.Status;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.service.EventService;
import ru.practicum.feign.CategoryClient;
import ru.practicum.feign.UserClient;

import java.util.List;

@RequiredArgsConstructor
@Component
public final class CompilationMapper {
    private final EventMapper eventMapper;
    private final UserClient userClient;
    private final CategoryClient categoryClient;
    private final EventService eventService;
    private final AnalyzerClient analyzerClient;

    public CompilationDto mapToDto(Compilation compilation) {
        return CompilationDto.builder()
                .events(compilation.getEvents().stream()
                        .map(event -> eventMapper.mapToShortDto(event, userClient.getUserById(event.getInitiatorId()),
                                categoryClient.findById(event.getCategoryId()),
                                eventService.countRequestConfirmedByEventDto(event.getId(), Status.CONFIRMED),
                                analyzerClient.getInteractionsCount(List.of(event.getId())).get(event.getId())))
                        .toList())
                .id(compilation.getId())
                .pinned(compilation.getPinned())
                .title(compilation.getTitle())
                .build();
    }

    public Compilation mapToCompilation(NewCompilationDto dto) {
        return Compilation.builder()
                .title(dto.getTitle())
                .pinned(dto.getPinned())
                .build();
    }

}
