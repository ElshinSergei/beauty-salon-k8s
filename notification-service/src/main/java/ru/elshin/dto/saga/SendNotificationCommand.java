package ru.elshin.dto.saga;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class SendNotificationCommand {
    private UUID correlationId;
    private Long appointmentId;
    private Long userId;
    private String message;
}
