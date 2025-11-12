package ru.practicum.event.service;

import feign.FeignException;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.client.AnalyzerClient;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.event.EventDto;
import ru.practicum.dto.request.Status;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.event.dto.*;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.dto.event.State;
import ru.practicum.event.model.StateAction;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.ewm.stats.grpc.predict.RecommendedEventProto;
import ru.practicum.exeption.ConflictException;
import ru.practicum.exeption.InvalidRequestException;
import ru.practicum.exeption.NotFoundException;
import ru.practicum.feign.CategoryClient;
import ru.practicum.feign.RequestClient;
import ru.practicum.feign.UserClient;
import ru.practicum.location.mapper.LocationMapper;
import ru.practicum.location.model.Location;
import ru.practicum.location.repository.LocationRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor(onConstructor_ = @Autowired)
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final LocationRepository locationRepository;
    private final UserClient userClient;
    private final CategoryClient categoryClient;
    private final EventMapper eventMapper;
    private final LocationMapper locationMapper;
    private final RequestClient requestClient;
    private final AnalyzerClient analyzerClient;

    @Transactional
    @Override
    public EventDto addEvent(Long userId, NewEventRequest request) {
        log.info("Начинаем создание мероприятия {} пользователем id = {}", request, userId);

        UserShortDto user = findUserById(userId);
        log.info("Проверка пользователя id = {} создателя мероприятия прошла успешно", userId);

        CategoryDto category = findCategoryById(request.getCategory());
        log.info("Проверка наличия категории id = {} прошла успешно", request.getCategory());

        Location location = locationRepository.save(locationMapper.mapToLocationNew(request.getLocation()));
        log.info("Создаем локацию меропрития {}", location);

        Event createEvent = eventMapper.mapToEventNew(request);
        createEvent.setCreatedOn(LocalDateTime.now());
        if (request.hasParticipantLimit()) {
            createEvent.setParticipantLimit(request.getParticipantLimit());
        } else {
            createEvent.setParticipantLimit(0);
        }
        createEvent.setLocation(location);
        createEvent.setInitiatorId(userId);
        createEvent.setState(State.PENDING);
        Event event = eventRepository.save(createEvent);
        log.info("Создание меропрития {} завершено", event);
        return eventMapper.mapToFullDto(event, user, category,
                countRequestConfirmedByEventDto(event.getId(), Status.CONFIRMED),
                getRatingByEvent(createEvent.getId()));
    }

    @Override
    @Transactional
    public EventDto updateEventByUser(Long userId, Long eventId, UpdateEventRequest request) {
        Event event = getEvent(eventId);
        log.info("Валидация события (id {}) для обновления пользователем (id {})", event.getId(), userId);
        if (!(userClient.userExistById(userId) &&
                event.getInitiatorId().equals(userId) &&
                !event.getState().equals(State.PUBLISHED))) {
            log.warn("Конфликт при запросе на обновление события");
            throw new ConflictException("Данное событие нельзя обновлять");
        }
        updateEventFields(event, request);

        return eventMapper.mapToFullDto(eventRepository.save(event), findUserById(userId),
                findCategoryById(event.getCategoryId()),
                countRequestConfirmedByEventDto(eventId, Status.CONFIRMED),
                getRatingByEvent(eventId));
    }

    @Override
    @Transactional
    public EventDto updateEventByAdmin(Long eventId, UpdateEventRequest request) {
        Event event = getEvent(eventId);
        log.info("Валидация события (id {}) для обновления", event.getId());
        if (request.getStateAction() != null
                && request.getStateAction().equals(StateAction.PUBLISH_EVENT.toString())
                && !event.getState().equals(State.PENDING)) {
            log.warn("Попытка публикации события, которое не в ожидании публикации");
            throw new ConflictException("Событие можно публиковать, только если оно в состоянии ожидания публикации");
        }
        if (request.getStateAction() != null
                && request.getStateAction().equals(StateAction.REJECT_EVENT.toString())
                && event.getState().equals(State.PUBLISHED)) {
            log.warn("Попытка отклонения события, которое уже опубликовано");
            throw new ConflictException("Событие можно отклонить, только если оно еще не опубликовано ");
        }
        updateEventFields(event, request);
        return eventMapper.mapToFullDto(eventRepository.save(event), findUserById(event.getInitiatorId()),
                findCategoryById(event.getCategoryId()),
                countRequestConfirmedByEventDto(eventId, Status.CONFIRMED),
                getRatingByEvent(eventId));
    }

    @Transactional
    @Override
    public EventDto getByIdPrivate(Long userId, Long eventId) {
        log.info("Начинаем получение мероприятия id = {}", eventId);
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event", eventId));
        log.info("Мероприятие успешно получено: {}", event);
        return eventMapper.mapToFullDto(event, findUserById(userId), findCategoryById(event.getCategoryId()),
                countRequestConfirmedByEventDto(eventId, Status.CONFIRMED),
                getRatingByEvent(eventId));
    }

    @Transactional
    @Override
    public EventDto getByIdPublic(Long eventId) {
        log.info("Начинаем поиск мероприятия id = {} со статусом Published", eventId);
        Event event = eventRepository.findByIdAndState(eventId, State.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event", eventId));
        log.info("Мероприятие найдено: {}", event);
        return eventMapper.mapToFullDto(event, findUserById(event.getInitiatorId()),
                findCategoryById(event.getCategoryId()),
                countRequestConfirmedByEventDto(eventId, Status.CONFIRMED),
                getRatingByEvent(eventId));
    }

    @Override
    public boolean getByEventIdAndUserId(Long eventId, Long userId) {
        log.info("Проверяем наличие в БД Event id = {} пользователя id = {}", eventId, userId);
        return eventRepository.existsByIdAndInitiatorId(eventId, userId);
    }

    @Override
    public EventDto getById(Long eventId) {
        log.info("Получение из БД Event id = {}", eventId);
        Event event = getEvent(eventId);
        log.info("Event id = {} найден", eventId);
        return eventMapper.mapToFullDto(event, findUserById(event.getInitiatorId()),
                findCategoryById(event.getCategoryId()),
                countRequestConfirmedByEventDto(eventId, Status.CONFIRMED),
                getRatingByEvent(eventId));
    }

    @Transactional
    @Override
    public List<EventShortDto> getUsersEvents(Long userId, Pageable page) {
        findUserById(userId);
        Page<Event> events = eventRepository.findByInitiatorId(userId, page);
        log.info("Получаем все опубликованные мероприятия для пользователя id = {}: размер списка: {}, " +
                "список меропритий: {}", userId, events.getSize(), events.getContent());
        return events.stream()
                .map(event -> eventMapper.mapToShortDto(event, findUserById(event.getInitiatorId()),
                        findCategoryById(event.getCategoryId()),
                        countRequestConfirmedByEventDto(event.getId(), Status.CONFIRMED),
                        getRatingByEvent(event.getId())))
                .sorted(Comparator.comparingLong(EventShortDto::getId))
                .toList();
    }

    @Transactional
    @Override
    public List<EventDto> getEventsWithParamAdmin(EventSearchParam eventSearchParam, Pageable page) {
        log.info("Начинаем получение событий с фильтрами для Admin API");
        Specification<Event> spec = createSpecification(eventSearchParam);
        List<Event> events = eventRepository.findAll(spec, page);
        if (events.isEmpty()) {
            log.info("Не найдено событий для поиска Admin API с фильтрами");
        }
        log.info("Возвращаем список мероприятий для Admin API: {}", events);
        return events.stream()
                .map(event -> eventMapper.mapToFullDto(event,
                        findUserById(event.getInitiatorId()),
                        findCategoryById(event.getCategoryId()),
                        countRequestByEventDto(event.getId()),
                        getRatingByEvent(event.getId())))
                .toList();
    }

    @Transactional
    @Override
    public List<EventShortDto> getEventsWithParamPublic(EventSearchParam eventSearchParam, Pageable page) {
        log.info("Начинаем получение событий с фильтрами для Public API");
        Specification<Event> spec = createSpecification(eventSearchParam);
        List<EventShortDto> events = eventRepository.findAll(spec, page).stream()
                .map(event -> eventMapper.mapToShortDto(event, findUserById(event.getInitiatorId()),
                        findCategoryById(event.getCategoryId()),
                        countRequestConfirmedByEventDto(event.getId(), Status.CONFIRMED),
                        getRatingByEvent(event.getId())))
                .toList();
        log.info("Возвращаем список мероприятий для Public API: {}", events);
        return events;
    }

    @Override
    public boolean existsByCategoryId(@RequestParam Long id) {
        return eventRepository.existsByCategoryId(id);
    }

    @Override
    public Long countRequestConfirmedByEventDto(Long eventId, Status status) {
        return requestClient.countRequestsByEventAndStatus(eventId, status);
    }

    @Override
    public void sendLike(Long eventId, Long userId) {
        try {
            requestClient.findByRequesterIdAndEventIdAndStatus(userId, eventId, Status.CONFIRMED);
        } catch (FeignException ex) {
            throw new InvalidRequestException("Нет подтвержденной заявки на мероприятие, невозможно поставить лайк");
        }
    }

    @Override
    public List<EventDto> getRecommendation(Long userId, int maxResult) {
        List<Long> ids = analyzerClient.getRecommendations(userId, maxResult).stream()
                .sorted((a, b) -> (int) (a.getScore() - b.getScore()))
                .map(RecommendedEventProto::getEventId).toList();
        return eventRepository.findAllById(ids).stream()
                .map(event -> eventMapper.mapToFullDto(event,
                        findUserById(event.getInitiatorId()),
                        findCategoryById(event.getCategoryId()),
                        countRequestByEventDto(event.getId()),
                        getRatingByEvent(event.getId())))
                .toList();
    }

    private double getRatingByEvent(Long id) {
        return analyzerClient.getInteractionsCount(List.of(id)).get(id);
    }

    private Long countRequestByEventDto(Long eventId) {
        return requestClient.countRequestByEvent(eventId);
    }

    private UserShortDto findUserById(Long id) {
        try {
            return userClient.getUserById(id);
        } catch (FeignException ex) {
            throw new NotFoundException("User", id);
        }
    }

    private CategoryDto findCategoryById(Long id) {
        try {
            return categoryClient.findById(id);
        } catch (FeignException ex) {
            throw new NotFoundException("Category", id);
        }
    }

    private Event getEvent(Long id) {
        log.info("Поиск мероприятия (id {})", id);
        return eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Событие", id));
    }

    private void updateEventFields(Event event, UpdateEventRequest request) {
        if (request.getAnnotation() != null) {
            log.debug("Обновление краткого описания события");
            event.setAnnotation(request.getAnnotation());
        }
        if (request.getCategory() != null) {
            Long id = request.getCategory().longValue();
            findCategoryById(id);
            event.setCategoryId(id);
            log.debug("Обновление категории события");
        }
        if (request.getDescription() != null) {
            log.debug("Обновление полного описания");
            event.setDescription(request.getDescription());
        }
        if (request.getEventDate() != null) {
            if (request.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                log.warn("Попытка публикации события c датой, не соответствующей требованиям: {}", request.getEventDate());
                throw new InvalidRequestException("Событие не может начинаться раньше, чем через 2 часа");
            }
            log.debug("Обновление даты и времени события");
            event.setEventDate(request.getEventDate());
        }
        if (request.getLocation() != null) {
            log.debug("Обновление места проведения события");
            Location newLocation = locationRepository.save(locationMapper.mapToFull(request.getLocation()));
            event.setLocation(newLocation);
        }
        if (request.getPaid() != null) {
            log.debug("Обновление поля необходимости оплаты события");
            event.setPaid(request.getPaid());
        }
        if (request.getParticipantLimit() != null) {
            if (request.getParticipantLimit() < 0) {
                throw new InvalidRequestException("Лимит участников не может быть отрицательным");
            }
            log.debug("Обновление лимита участников события");
            event.setParticipantLimit(request.getParticipantLimit());
        }
        if (request.getRequestModeration() != null) {
            log.debug("Обновление статуса пре-модерации для события");
            event.setRequestModeration(request.getRequestModeration());
        }
        if (request.getStateAction() != null) {
            log.debug("Обновление статуса события");
            switch (request.getStateAction()) {
                case "SEND_TO_REVIEW":
                    event.setState(State.PENDING);
                    break;
                case "PUBLISH_EVENT":
                    event.setState(State.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                    break;
                default:
                    event.setState(State.CANCELED);
            }
        }
        if (request.getTitle() != null) {
            log.debug("Обновление заголовка события");
            event.setTitle(request.getTitle());
        }
    }

    private Specification<Event> createSpecification(final EventSearchParam searchParam) {
        LocalDateTime rangeStart = searchParam.getRangeStart();
        LocalDateTime rangeEnd = searchParam.getRangeEnd();
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            log.error("Конец выборки находится во временной шкале раньше начала");
            throw new InvalidRequestException("Время окончания выборки не может быть раньше времени начала");
        }
        return (root, query, cb) -> {
            log.info("Начинаем фильтрацию переданных параметров");
            List<Predicate> predicates = new ArrayList<>();
            List<String> predicateLogs = new ArrayList<>();

            log.info("Проводим фильтрацию по пользователям {}", searchParam.getUsers());
            if (searchParam.getUsers() != null && !searchParam.getUsers().isEmpty() &&
                    searchParam.getUsers().getFirst() != 0) {
                predicateLogs.add("Пользователи: " + searchParam.getUsers());
                predicates.add(root.get("initiatorId").in(searchParam.getUsers()));
            }

            log.info("Проводим фильтрацию по состояниям {}", searchParam.getStates());
            if (searchParam.getStates() != null && !searchParam.getStates().isEmpty()) {
                predicateLogs.add("Статусы: " + searchParam.getStates());
                predicates.add(root.get("state").in(searchParam.getStates()));
            }

            log.info("Проводим фильтрацию по категориям {}", searchParam.getCategories());
            if (searchParam.getCategories() != null && !searchParam.getCategories().isEmpty() &&
                    searchParam.getCategories().getFirst() != 0) {
                predicateLogs.add("Категории: " + searchParam.getCategories());
                predicates.add(root.get("categoryId").in(searchParam.getCategories()));
            }

            log.info("Проводим фильтарацию по временным рамкам: {}, {}", rangeStart, rangeEnd);
            if (rangeStart != null && rangeEnd != null) {
                log.info("Фильтрация по времени начала мероприятия, начальное время: {} конечное время {}",
                        rangeStart, rangeEnd);
                predicateLogs.add("Временной диапазон: от " + rangeStart + " до " + rangeEnd);
                predicates.add(cb.between(root.get("eventDate"), rangeStart, rangeEnd));
            } else {
                if (rangeStart != null) {
                    log.info("Время окончания выборки не указано, проводим фильтрацию по всем событиям от {}", rangeStart);
                    predicateLogs.add("Время начала выборки: " + rangeStart);
                    predicates.add(cb.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
                }

                if (rangeEnd != null) {
                    log.info("Время начала выборки не указано, проводим фильтрацию по всем событиям до {}", rangeEnd);
                    predicateLogs.add("Время окончания выборки: " + rangeEnd);
                    predicates.add(cb.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
                }

                if (rangeStart == null && rangeEnd == null) {
                    log.info("Диапазон не указан, выборка по всем предстоящим событиям");
                    predicateLogs.add("Время выборки не указано, выборка по предстоящим событиям от: " + LocalDateTime.now());
                    predicates.add(cb.greaterThan(root.get("eventDate"), cb.currentTimestamp()));
                }
            }

            log.info("Проводим текстовый поиск по запросу {}", searchParam.getText());
            if (searchParam.getText() != null) {
                String searchText = searchParam.getText().toLowerCase();

                if (searchText.isEmpty() || searchText.isBlank()) {
                    log.info("Текстовый поиск не выполняется - строка пуста");
                } else if ("0".equals(searchText)) {
                    log.info("Обнаружено значение '0' в текстовом поиске");
                } else {
                    log.info("Создание предиката для поиска по текстовому запросу в аннотации");
                    Predicate annotationPredicate = cb.like(
                            cb.lower(root.get("annotation")),
                            "%" + searchText + "%"
                    );

                    log.info("Создание предиката для поиска по текстовому запросу в описании");
                    Predicate descriptionPredicate = cb.like(
                            cb.lower(root.get("description")),
                            "%" + searchText + "%"
                    );
                    predicateLogs.add("Поиск по текстовому запросу: " + searchParam.getText());
                    log.info("Объединяем предикаты для поиска в обоих полях");
                    predicates.add(cb.or(annotationPredicate, descriptionPredicate));
                }
            }

            log.info("Добавляем сортировку по параметру {}", searchParam.getSort());
            if (searchParam.getSort() != null) {
                switch (searchParam.getSort().toUpperCase()) {
                    case "EVENT_DATE":
                        predicateLogs.add("Поиск с сортировкой по: EVENT_DATE");
                        query.orderBy(cb.asc(root.get("eventDate")));
                        break;
                    case "VIEWS":
                        predicateLogs.add("Поиск с сортировкой по: VIEWS");
                        query.orderBy(cb.asc(root.get("views")));
                        break;
                }
            }

            log.info("Проводим поиск по paid {}", searchParam.getPaid());
            if (searchParam.getPaid() != null) {
                predicateLogs.add("Поиск по paid с флагом: " + searchParam.getPaid());
                predicates.add(cb.lessThanOrEqualTo(root.get("paid"), searchParam.getPaid()));
            }

            log.info("Проводим поиск по onlyAvailable {}", searchParam.getOnlyAvailable());
            if (searchParam.getOnlyAvailable() != null && searchParam.getOnlyAvailable()) {
                Expression<Integer> limitExpr = root.get("participantLimit");
                Expression<Integer> requestsExpr = root.get("confirmedRequests");
                Predicate limitNotZero = cb.notEqual(limitExpr, 0);
                if (limitNotZero != null) {
                    predicateLogs.add("Поиск при participantLimit больше 0 и onlyAvailable с флагом: "
                            + searchParam.getOnlyAvailable());
                    predicates.add(cb.lessThan(requestsExpr, limitExpr));
                }
            }

            log.info("Количество собранных предикатов {}", predicates.size());
            log.info("Возвращаем лист с параметрами поиска: {}", predicateLogs);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
