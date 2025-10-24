package ru.practicum.event.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.dto.event.State;
import ru.practicum.location.model.Location;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "events", indexes = {
        @Index(name = "idx_event_annotation", columnList = "annotation"),
        @Index(name = "idx_event_description", columnList = "description")
})
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "title", nullable = false)
    String title;

    @Column(name = "annotation")
    String annotation;

    @Column(name = "description", nullable = false)
    String description;

    @Column(name = "event_date", nullable = false)
    LocalDateTime eventDate;

    @JoinColumn(name = "category_id")
    Long categoryId;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "location_id")
    Location location;

    @Column(name = "paid")
    Boolean paid;

    @Column(name = "participant_limit")
    int participantLimit;

    @Column(name = "request_moderation")
    Boolean requestModeration;

    @JoinColumn(name = "initiator_id")
    Long initiatorId;

    @Column(name = "views")
    int views;

    @Enumerated(EnumType.STRING)
    State state;

    @Column(name = "created_on")
    LocalDateTime createdOn;

    @Column(name = "published_on")
    LocalDateTime publishedOn;
}
