package ru.elshin.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import ru.elshin.config.RabbitMQConfig;
import ru.elshin.dto.saga.VerifyUserCommand;
import ru.elshin.dto.saga.UserVerifiedEvent;
import ru.elshin.dto.saga.UserVerificationFailedEvent;
import ru.elshin.service.UserService;
import ru.elshin.exception.ResourceNotFoundException;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserSagaListener {

    private final UserService userService;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMQConfig.VERIFY_USER_QUEUE)
    public void handleVerifyUserCommand(VerifyUserCommand command) {
        log.info("Received VerifyUserCommand: correlationId={}, appointmentId={}, userId={}",
                command.getCorrelationId(), command.getAppointmentId(), command.getUserId());

        try {
            // Проверяем пользователя
            userService.getUserById(command.getUserId());

            // Если успех, шлем событие
            UserVerifiedEvent event = UserVerifiedEvent.builder()
                    .correlationId(command.getCorrelationId())
                    .appointmentId(command.getAppointmentId())
                    .build();

            rabbitTemplate.convertAndSend(RabbitMQConfig.USER_VERIFICATION_EXCHANGE, "user.verified.success", event);
            log.info("Sent UserVerifiedEvent for appointment: {}", command.getAppointmentId());

        } catch (ResourceNotFoundException e) {
            // Если не найден, шлем отказ
            UserVerificationFailedEvent event = UserVerificationFailedEvent.builder()
                    .correlationId(command.getCorrelationId())
                    .appointmentId(command.getAppointmentId())
                    .reason("Пользователь не найден")
                    .build();

            rabbitTemplate.convertAndSend(RabbitMQConfig.USER_VERIFICATION_EXCHANGE, "user.verified.failed", event);
            log.warn("Sent UserVerificationFailedEvent for appointment: {}. Reason: {}", command.getAppointmentId(), e.getMessage());
        }
    }
}
