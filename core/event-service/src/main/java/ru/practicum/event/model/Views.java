package ru.practicum.event.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "views")
public class Views {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "event_id", nullable = false)
    Long eventId;

    @Column(name = "ip", nullable = false)
    String ip;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Views views = (Views) o;
        return Objects.equals(id, views.id) && Objects.equals(eventId, views.eventId) && Objects.equals(ip, views.ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, eventId, ip);
    }

    @Override
    public String toString() {
        return "Views{" +
                "id=" + id +
                ", eventId=" + eventId +
                ", ip='" + ip + '\'' +
                '}';
    }
}
