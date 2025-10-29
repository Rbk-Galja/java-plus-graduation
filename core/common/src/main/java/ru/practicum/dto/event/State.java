package ru.practicum.dto.event;

public enum State {
    //ожидает публикации
    PENDING,

    //опубликовано, редактировать уже нельзя
    PUBLISHED,

    //отменено, пользователям не отображать, нельзя редактировать
    CANCELED
}
