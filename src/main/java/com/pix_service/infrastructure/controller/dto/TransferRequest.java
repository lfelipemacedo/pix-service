package com.pix_service.infrastructure.controller.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(UUID senderId, String pixKey, BigDecimal amount) {
}
