package com.pix_service.application.wallet.command;

import com.pix_service.shared.application.Command;

import java.util.UUID;

public record AddPixKeyCommand(UUID walletId, String pixKey) implements Command<Void> {
    public static AddPixKeyCommand with(String walletId, String pixKey) {
        return new AddPixKeyCommand(UUID.fromString(walletId), pixKey);
    }
}
