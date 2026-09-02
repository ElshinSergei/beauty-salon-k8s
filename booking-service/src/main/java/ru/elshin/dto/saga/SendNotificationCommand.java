package ru.elshin.dto.saga;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendNotificationCommand {
    private UUID correlationId;
    private Long appointmentId;
    private Long userId;
    private String message;
}
