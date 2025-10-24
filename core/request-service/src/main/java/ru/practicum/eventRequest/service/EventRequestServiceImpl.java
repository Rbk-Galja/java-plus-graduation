package ru.practicum.eventRequest.service;

import feign.FeignException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.event.EventDto;
import ru.practicum.dto.event.State;
import ru.practicum.dto.request.EventRequestDto;
import ru.practicum.eventRequest.dto.EventRequestUpdateDto;
import ru.practicum.eventRequest.dto.EventRequestUpdateResult;
import ru.practicum.eventRequest.feign.EventClient;
import ru.practicum.eventRequest.feign.UserClient;
import ru.practicum.eventRequest.mapper.EventRequestMapper;
import ru.practicum.eventRequest.model.EventRequest;
import ru.practicum.dto.request.Status;
import ru.practicum.eventRequest.repository.EventRequestRepository;
import ru.practicum.exeption.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventRequestServiceImpl implements EventRequestService {
    private final EventClient eventClient;
    private final UserClient userClient;
    private final EventRequestRepository eventRequestRepository;
    private final EventRequestMapper mapper;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<EventRequestDto> getUsersRequests(Long userId) {
        findUserById(userId);
        return eventRequestRepository.findAllByRequesterId(userId).stream()
                .map(mapper::mapToEventRequestDto).toList();
    }

    @Transactional
    @Override
    public EventRequestDto createRequest(Long userId, Long eventId) {
        log.info("Начинаем создание заявки на участие в мероприятии id = {} от пользователя id = {}", eventId, userId);
        findUserById(userId);
        EventDto event = findEventById(eventId);
        Optional<EventRequest> request = eventRequestRepository.findByEventIdAndRequesterId(eventId, userId);
        if (request.isPresent()) {
            throw new RequestModerationException(eventId, "Заявка уже была отправлена");
        }

        if (event.getParticipantLimit() > 0 && event.getParticipantLimit() ==
                eventRequestRepository.countRequestsByEventAndStatus(eventId, Status.CONFIRMED)) {
            log.error("Заявка не была добавлена: лимит заявок исчерпан: лимит={}, принятых заявок={}",
                    event.getParticipantLimit(), event.getConfirmedRequests());
            throw new RequestModerationException(eventId, "Лимит заявок исчерпан");
        }
        if (eventClient.findByIdAndInitiatorId(eventId, userId)) {
            log.error("Заявка не была отправлена: нельзя отправить заявку на собственное мероприятие");
            throw new RequestModerationException(eventId, "Нельзя отправить заявку на собственное мероприятие");
        }
        if (!event.getState().equals(State.PUBLISHED)) {
            log.error("Заявка не была отправлена: нельзя отправить заявку на неопубликованное мероприятие: {}",
                    event.getState());
            throw new RequestModerationException(eventId, "Нельзя отправить заявку на неопубликованное мероприятие");
        }

        EventRequest eventRequest = EventRequest.builder()
                .created(LocalDateTime.now())
                .requesterId(userId)
                .eventId(event.getId())
                .status(Status.PENDING)
                .build();
        if (event.getParticipantLimit() == 0) {
            log.info("Статус заявки автоматически изменен на CONFIRMED, лимит заявок для Event id={} не установлен",
                    eventId);
            eventRequest.setStatus(Status.CONFIRMED);
            log.info("Подтверждение заявки на событие с id {}", eventRequest.getEventId());
        }

        if (!event.getRequestModeration()) {
            log.info("Статус заявки автоматически изменен на CONFIRMED, модерация заявок для Event id={} не установлена",
                    eventId);
            eventRequest.setStatus(Status.CONFIRMED);
            log.info("Подтверждение заявки на событие с id {}", eventRequest.getEventId());
        }
        EventRequest savedRequest = eventRequestRepository.save(eventRequest);
        log.info("Заявка успешно сохранена: {}", savedRequest);
        return mapper.mapToEventRequestDto(savedRequest);
    }

    @Override
    @Transactional
    public EventRequestDto cancelRequest(Long userId, Long requestId) {
        log.info("Отменяем заявку id={} пользователем id={}", requestId, userId);
        EventRequest eventRequest = eventRequestRepository.findById(requestId).orElseThrow(() ->
                new NotFoundException("EventRequest", requestId));
        if (!eventRequest.getRequesterId().equals(userId)) {
            throw new NotValidUserException(userId);
        }
        int i = eventRequestRepository.updateStatus(requestId, Status.CANCELED);
        if (i == 1) {
            log.info("Заявка отменена");
            eventRequest.setStatus(Status.CANCELED);
        }
        return mapper.mapToEventRequestDto(eventRequest);
    }

    @Transactional
    @Override
    public List<EventRequestDto> getAllByEventId(Long userId, Long eventId) {
        log.info("Поиск заявок на участие от пользователя id={} для Event id={}", userId, eventId);
        findUserById(userId);
        findEventById(eventId);
        return eventRequestRepository.findAllByEventId(eventId).stream()
                .map(mapper::mapToEventRequestDto)
                .toList();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public EventRequestUpdateResult updateRequestState(Long userId, Long eventId,
                                                       EventRequestUpdateDto updateDto) {
        log.info("Начинаем обновление заявок для событий id={} пользователем id={}", eventId, userId);

        EventRequestUpdateResult result = EventRequestUpdateResult.builder().build();

        String status = updateDto.getStatus();
        List<Long> requestIds = updateDto.getRequestIds();

        findUserById(userId);
        log.info("Определен инициатор события {}", userId);
        EventDto event = findEventById(eventId);
        log.info("Определен Event: {}", event);

        if (!event.getInitiator().getId().equals(userId)) {
            log.error("Пользователь id={} не является инициатором события id={}", userId, eventId);
            throw new ConflictException("У пользователя нет доступа к данному событию");
        }

        List<EventRequest> requests = eventRequestRepository.findByRequestIds(requestIds);
        if (requests.stream().anyMatch(eventRequest -> !eventRequest.getStatus().equals(Status.PENDING))) {
            log.error("В списке есть заявка не находящаяся в статусе ожидания");
            throw new RequestModerationException(eventId, "Можно принимать заявки только в статусе ожидания");
        }

        if (status.equalsIgnoreCase("rejected")) {
            updateStatusAllRequest(requestIds, Status.REJECTED);
            result.setRejectedRequests(findAllByListIds(requestIds));
            return result;
        }

        Long limit = Long.valueOf(event.getParticipantLimit());
        Long confirmed = eventRequestRepository.countRequestsByEventAndStatus(eventId, Status.CONFIRMED);

        log.info("Проверяем наличие свободных мест");
        if (limit > 0 && limit <= confirmed) {
            log.error("Лимит заявок для мероприятия id={} уже исчерпан {}", eventId, limit - confirmed);
            throw new RequestModerationException(eventId, "Лимит заявок исчерпан");
        }

        int checkLimit = (int) (limit - confirmed);
        log.info("Вычисляем количество свободных мест {}", checkLimit);

        List<Long> toConfirm;
        List<Long> toReject = List.of();

        if (requests.size() > checkLimit) {
            toConfirm = requestIds.subList(0, checkLimit);
            toReject = requestIds.subList(checkLimit, requestIds.size());
        } else {
            toConfirm = requestIds;
        }

        if (!toConfirm.isEmpty()) {
            updateStatusAllRequest(toConfirm, Status.CONFIRMED);
            result.setConfirmedRequests(findAllByListIds(toConfirm));
        }
        if (!toReject.isEmpty()) {
            updateStatusAllRequest(toReject, Status.REJECTED);
            result.setRejectedRequests(findAllByListIds(toReject));
        }
        return result;
    }

    @Override
    public boolean findByRequesterIdAndEventIdAndStatus(Long requesterId, Long eventId, Status status) {
        return eventRequestRepository.findByRequesterIdAndEventIdAndStatus(requesterId, eventId, status);
    }

    @Override
    public Long countRequestsByEventAndStatus(Long id, Status status) {
        return eventRequestRepository.countRequestsByEventAndStatus(id, status);
    }

    @Override
    public Long countRequestByEvent(Long id) {
        return eventRequestRepository.countRequestByEvent(id);
    }

    @Transactional
    private List<EventRequestDto> findAllByListIds(List<Long> ids) {
        log.info("Получаем все обновленные заявки по id={}", ids);
        entityManager.clear();
        List<EventRequest> requests = eventRequestRepository.findByIdIn(ids);
        log.info("Возвращаем список обновленных заявок {}", requests);
        return requests.stream()
                .map(mapper::mapToEventRequestDto)
                .toList();
    }

    @Transactional
    private void updateStatusAllRequest(List<Long> ids, Status status) {
        log.info("Обновляем статус для заявок id={} на {}", ids, status);
        int update = eventRequestRepository.updateStatusForRequestsIds(ids, status);
        entityManager.flush();
        log.info("Количество обновленных записей: {}", update);
    }

    private void findUserById(Long id) {
        try {
            userClient.getUserById(id);
        } catch (FeignException ex) {
            throw new NotFoundException("User", id);
        }
    }

    private EventDto findEventById(Long id) {
        try {
            return eventClient.findById(id);
        } catch (FeignException ex) {
            throw new NotFoundException("Event", id);
        }
    }
}
