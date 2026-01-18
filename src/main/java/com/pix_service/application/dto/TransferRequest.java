package com.pix_service.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(UUID senderId, String pixKey, BigDecimal amount) {
}
