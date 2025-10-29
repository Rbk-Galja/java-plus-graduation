package ru.practicum.dto.request;

public enum Status {
    //Заявка ожидает ответа от организатора
    PENDING,
    //Заявка принята
    CONFIRMED,
    //Заявка отклонена модератором
    REJECTED,
    //Заявка отменна создателем
    CANCELED
}
