package ru.elshin.dto.saga;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyUserCommand {
    private UUID correlationId;
    private Long appointmentId;
    private Long userId;
}
