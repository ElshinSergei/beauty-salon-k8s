package ru.elshin.service;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.elshin.client.UserClient;
import ru.elshin.dto.UserDto;
import ru.elshin.entity.Appointment;
import ru.elshin.entity.AppointmentStatus;
import ru.elshin.exception.AppointmentConflictException;
import ru.elshin.exception.ResourceNotFoundException;
import ru.elshin.repository.AppointmentRepository;
import ru.elshin.workflow.NotificationWorkflow;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserClient userClient; // Внедряем наш Feign-клиент
    private final WorkflowClient workflowClient;

    @CacheEvict(value = "appointments_by_user", key = "#appointment.clientId")
    @Transactional
    public Appointment createAppointment(Appointment appointment) {
        // 1. Проверяем существование клиента в user-service
        UserDto client = userClient.getUserById(appointment.getClientId());
        // 2. Проверяем существование мастера в user-service
        UserDto master = userClient.getUserById(appointment.getMasterId());
        // 3. Проверяем, действительно ли у мастера роль MASTER
        if (!"MASTER".equalsIgnoreCase(master.getRole())) {
            throw new AppointmentConflictException("Пользователь с ID " + appointment.getMasterId() + " не является мастером!");
        }
        // 4. Проверяем занятость мастера на это время
        boolean isTimeBusy = appointmentRepository.existsByMasterIdAndAppointmentTime(
                appointment.getMasterId(),
                appointment.getAppointmentTime()
        );

        if (isTimeBusy) {
            throw new AppointmentConflictException("Мастер уже занят на это время!");
        }

        appointment.setStatus(AppointmentStatus.PENDING);
        Appointment savedAppointment = appointmentRepository.save(appointment);

        NotificationWorkflow workflow = workflowClient.newWorkflowStub(
                NotificationWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue("NOTIFICATION_TASK_QUEUE")
                        .build());

        // Запускаем асинхронно
        WorkflowClient.start(workflow::sendNotification, "Запись создана", savedAppointment.getClientId().toString());

        return savedAppointment;
    }

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }


    // метод для получения ВСЕХ записей мастера
    @Cacheable(value = "appointments_by_master", key = "#masterId")
    public List<Appointment> getAppointmentsByMaster(Long masterId) {
        return appointmentRepository.findByMasterId(masterId);
    }

    // метод для фильтрации по конкретному дню
    public List<Appointment> getAppointmentsByMasterAndDate(Long masterId, LocalDate date) {
        // Начало дня: 2026-07-20T00:00
        LocalDateTime startOfDay = date.atStartOfDay();
        // Конец дня: 2026-07-20T23:59:59.999999999
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        return appointmentRepository.findByMasterIdAndAppointmentTimeBetween(masterId, startOfDay, endOfDay);
    }

    /**
     * Подтверждение записи мастером
     */
    // При подтверждении/отмене: очищаем кеш для мастера И клиента
    @CacheEvict(value = {"appointments_by_master", "appointments_by_user"}, allEntries = true)
    @Transactional
    public Appointment confirmAppointment(Long id, Long currentUserId) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Запись с ID " + id + " не найдена"));

        // Проверяем, что подтвердить запись может ТОЛЬКО мастер этой записи
        if (!appointment.getMasterId().equals(currentUserId)) {
            throw new AppointmentConflictException("Только мастер этой записи может подтвердить её!");
        }

        // Валидация: подтвердить можно только запись в статусе PENDING
        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new AppointmentConflictException(
                    "Нельзя подтвердить запись в статусе " + appointment.getStatus()
            );
        }

        String previousStatus = appointment.getStatus().name();
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        Appointment updated = appointmentRepository.save(appointment);


        return updated;
    }

    /**
     * Отмена записи
     */
    @Transactional
    @CacheEvict(value = {"appointments_by_master", "appointments_by_user"}, allEntries = true)
    public Appointment cancelAppointment(Long id, Long currentUserId) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Запись с ID " + id + " не найдена"));

        // Проверяем права на отмену (клиент или мастер)
        if (!appointment.getClientId().equals(currentUserId) && !appointment.getMasterId().equals(currentUserId)) {
            throw new AppointmentConflictException("У вас нет прав для отмены этой записи");
        }

        // Валидация: нельзя отменить то, что уже выполнено (COMPLETED) или отменено (CANCELLED)
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            throw new AppointmentConflictException("Нельзя отменить уже выполненную запись!");
        }
        if (appointment.getStatus() == AppointmentStatus.CANCELLED) {
            throw new AppointmentConflictException("Запись уже была отменена ранее!");
        }

        String previousStatus = appointment.getStatus().name();
        appointment.setStatus(AppointmentStatus.CANCELLED);
        Appointment updated = appointmentRepository.save(appointment);

        return updated;
    }

    @Cacheable(value = "appointments_by_user", key = "#userId")
    public List<Appointment> getAppointmentsByUserId(Long userId) {
        return appointmentRepository.findByClientId(userId);
    }

}
