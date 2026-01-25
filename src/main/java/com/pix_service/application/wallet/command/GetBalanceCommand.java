package com.pix_service.application.wallet.command;

import com.pix_service.shared.application.Command;

import java.math.BigDecimal;
import java.util.UUID;

public record GetBalanceCommand(UUID walletId) implements Command<BigDecimal> {
    public static GetBalanceCommand with(String walletId) {
        return new GetBalanceCommand(UUID.fromString(walletId));
    }
}
