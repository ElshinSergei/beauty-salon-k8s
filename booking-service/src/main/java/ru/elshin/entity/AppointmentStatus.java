package ru.elshin.entity;

public enum AppointmentStatus {
    PENDING,   // Ожидает подтверждения
    VERIFIED,
    CONFIRMED, // Подтверждена
    CANCELLED, // Отменена
    COMPLETED  // Завершена
}
