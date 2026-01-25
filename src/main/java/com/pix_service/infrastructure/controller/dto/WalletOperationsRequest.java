package com.pix_service.infrastructure.controller.dto;

import java.math.BigDecimal;

public record WalletOperationsRequest(BigDecimal amount) {
    public static WalletOperationsRequest with(BigDecimal amount) {
        return new WalletOperationsRequest(amount);
    }
}
