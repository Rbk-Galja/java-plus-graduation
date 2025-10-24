package ru.practicum.comment.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.comment.model.Comment;
import ru.practicum.dto.event.State;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /* все комментарии автора */
    List<Comment> findByAuthorIdAndStatus(Long authorId, State status);

    /* все комментарии под событием */
    List<Comment> findByEventIdAndStatus(Long eventId, State status);

    @Modifying
    @Query("UPDATE Comment c SET c.text = :text WHERE c.id = :id")
    int updateCommentText(@Param("text") String text,
                          @Param("id") Long id);

    @Modifying
    @Query("UPDATE Comment c SET c.status = :status WHERE c.id IN (:ids)")
    void incrementStatus(@Param("status") State status,
                         @Param("ids") List<Long> ids);

    @Query("SELECT c FROM Comment c WHERE c.id IN (:ids)")
    List<Comment> findByIdIn(@Param("ids") List<Long> ids);

    List<Comment> findAll(Specification<Comment> spec, Pageable pageable);
}
