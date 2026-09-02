package ru.elshin.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import ru.elshin.config.RabbitMQConfig;
import ru.elshin.dto.saga.UserVerificationFailedEvent;
import ru.elshin.dto.saga.UserVerifiedEvent;
import ru.elshin.service.AppointmentService;

@Slf4j
@Component
@RequiredArgsConstructor
public class SagaReplyListener {

    private final AppointmentService appointmentService;

    @RabbitListener(queues = RabbitMQConfig.USER_VERIFICATION_QUEUE)
    public void handleUserVerified(UserVerifiedEvent event) {
        log.info("Received UserVerifiedEvent for appointment: {}", event.getAppointmentId());
        appointmentService.handleUserVerified(event);
    }

    @RabbitListener(queues = RabbitMQConfig.USER_VERIFICATION_QUEUE)
    public void handleUserVerificationFailed(UserVerificationFailedEvent event) {
        log.warn("Received UserVerificationFailedEvent for appointment: {}. Reason: {}",
                event.getAppointmentId(), event.getReason());
        appointmentService.handleUserVerificationFailed(event);
    }
}
