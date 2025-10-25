package ru.practicum.eventRequest.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.dto.request.Status;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "requests")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "event_id")
    private Long eventId;

    @JoinColumn(name = "requester_id")
    private Long requesterId;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "created")
    private LocalDateTime created;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventRequest that = (EventRequest) o;
        return Objects.equals(id, that.id) && Objects.equals(eventId, that.eventId)
                && Objects.equals(requesterId, that.requesterId) && status == that.status
                && Objects.equals(created, that.created);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, eventId, requesterId, status, created);
    }

    @Override
    public String toString() {
        return "EventRequest{" +
                "id=" + id +
                ", eventId=" + eventId +
                ", requesterId=" + requesterId +
                ", status=" + status +
                ", created=" + created +
                '}';
    }
}
