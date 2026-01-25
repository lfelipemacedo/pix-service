package com.pix_service.application.wallet.command;

import com.pix_service.shared.application.Command;

import java.math.BigDecimal;
import java.util.UUID;

public record WalletOperationsCommand(UUID id, BigDecimal amount, String type) implements Command<Void> {
    public static WalletOperationsCommand with(String walletId, BigDecimal amount, String type) {
        return new WalletOperationsCommand(UUID.fromString(walletId), amount, type);
    }
}
