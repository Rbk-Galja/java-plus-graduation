package ru.practicum.models;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "similarities")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventSimilarity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "event_a")
    Long eventA;

    @Column(name = "event_b")
    Long eventB;

    @Column(name = "score")
    Double score;

    @Column(name = "timestamp")
    LocalDateTime timestamp;
}
