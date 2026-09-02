package ru.elshin.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import ru.elshin.config.RabbitMQConfig;
import ru.elshin.dto.saga.SendNotificationCommand;

@Slf4j
@Component
public class NotificationSagaListener {

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_COMMAND_QUEUE)
    public void handleSendNotificationCommand(SendNotificationCommand command) {
        log.info("Сага: Принята команда на отправку уведомления. CorrelationId: {}", command.getCorrelationId());

        // Здесь логика реальной отправки (email, telegram и т.д.)
        log.info("Отправка уведомления пользователю {} по брони {}: '{}'",
                command.getUserId(), command.getAppointmentId(), command.getMessage());

        log.info("Сага успешно завершена для брони {}", command.getAppointmentId());
    }
}
