package ru.practicum.event.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.client.CollectorClient;
import ru.practicum.dto.event.EventDto;
import ru.practicum.event.dto.EventSearchParam;
import ru.practicum.event.dto.EventShortDto;
import ru.practicum.event.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class PublicEventController {
    private final EventService eventService;
    private final CollectorClient collectorClient;

    @GetMapping("/{eventId}")
    public EventDto getById(@RequestHeader("X-EWM-USER-ID") long userId, @PathVariable Long eventId) {
        log.info("Получаем мероприятие для Public API по id = {}", eventId);
        collectorClient.sendEventView(userId, eventId);
        log.info("Отправляем данные в collectorClient");
        return eventService.getByIdPublic(eventId);
    }

    @GetMapping
    public List<EventShortDto> getEventsWithParam(@RequestParam(required = false) String text,
                                                  @RequestParam(required = false) List<Long> categories,
                                                  @RequestParam(required = false) Boolean paid,
                                                  @RequestParam(required = false)
                                                  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                                  @RequestParam(required = false)
                                                  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                                  @RequestParam(defaultValue = "false")
                                                  Boolean onlyAvailable,
                                                  @RequestParam(required = false) String sort,
                                                  @RequestParam(defaultValue = "0")
                                                  @PositiveOrZero Integer from,
                                                  @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("Получаем мероприятия с фильтрацией");
        Pageable page = PageRequest.of(from, size);
        EventSearchParam eventSearchParam = EventSearchParam.builder()
                .text(text)
                .states(List.of("PUBLISHED"))
                .categories(categories)
                .paid(paid)
                .rangeStart(rangeStart)
                .rangeEnd(rangeEnd)
                .onlyAvailable(onlyAvailable)
                .sort(sort)
                .build();
        return eventService.getEventsWithParamPublic(eventSearchParam, page);
    }

    @PutMapping("/{eventId}/like")
    public void sendLike(@PathVariable Long eventId, @RequestHeader("X-EWM-USER-ID") long userId) {
        log.info("Добавляем лайк для мероприятия id={} от пользователя id={}", eventId, userId);
        eventService.sendLike(eventId, userId);
        collectorClient.sendEventLike(userId, eventId);
    }

    @GetMapping("/recommendations")
    public List<EventDto> getRecommendation(@RequestHeader("X-EWM-USER-ID") Long userId,
                                            @RequestParam Integer maxResults) {
        log.info("Возвращаем рекомендации для пользователя id = {}", userId);
        return eventService.getRecommendation(userId, maxResults);
    }
}
