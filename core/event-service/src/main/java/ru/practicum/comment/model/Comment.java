package ru.practicum.comment.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.event.model.Event;
import ru.practicum.dto.event.State;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "comments")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(length = 500, nullable = false)
    String text;

    @Column(nullable = false)
    LocalDateTime created;

    @JoinColumn(name = "author_id", nullable = false)
    Long authorId;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    Event event;

    @Enumerated(EnumType.STRING)
    State status;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Comment comment = (Comment) o;
        return Objects.equals(id, comment.id) && Objects.equals(text, comment.text)
                && Objects.equals(created, comment.created) && Objects.equals(authorId, comment.authorId)
                && Objects.equals(event, comment.event) && status == comment.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, text, created, authorId, event, status);
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", created=" + created +
                ", authorId=" + authorId +
                ", event=" + event +
                ", status=" + status +
                '}';
    }
}
