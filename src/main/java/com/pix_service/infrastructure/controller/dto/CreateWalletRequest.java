package com.pix_service.infrastructure.controller.dto;

import java.math.BigDecimal;

public record CreateWalletRequest(String pixKey, BigDecimal balance) {
    public static CreateWalletRequest with(String pixKey, BigDecimal balance) {
        return new CreateWalletRequest(pixKey, balance);
    }
}
