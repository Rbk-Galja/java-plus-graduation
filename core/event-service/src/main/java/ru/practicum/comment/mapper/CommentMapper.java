package ru.practicum.comment.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.comment.dto.CommentDto;
import ru.practicum.comment.model.Comment;
import ru.practicum.helper.RequestParamHelper;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "created", dateFormat = RequestParamHelper.DATE_TIME_FORMAT)
    @Mapping(target = "eventId", expression = "java(comment.getEvent().getId())")
    @Mapping(target = "status", expression = "java(comment.getStatus().name())")
    CommentDto toDto(Comment comment);
}
