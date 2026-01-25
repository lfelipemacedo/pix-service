package com.pix_service.application.wallet.command;

import com.pix_service.shared.application.Command;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateWalletCommand(String pixKey, BigDecimal initialBalance) implements Command<UUID> {
    public static CreateWalletCommand with(String pixKey, BigDecimal balance) {
        return new CreateWalletCommand(pixKey, balance);
    }
}
