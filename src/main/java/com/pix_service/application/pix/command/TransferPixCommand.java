package com.pix_service.application.pix.command;

import com.pix_service.application.pix.dto.TransferPixResponse;
import com.pix_service.shared.application.Command;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferPixCommand(
        UUID idempotencyKey,
        UUID senderId,
        String pixKey,
        BigDecimal amount) implements Command<TransferPixResponse> {
    public static TransferPixCommand with(UUID idempotencyKey, UUID senderId, String pixKey, BigDecimal amount) {
        return new TransferPixCommand(idempotencyKey, senderId, pixKey, amount);
    }
}
