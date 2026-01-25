package com.pix_service.application.wallet.command.handler;

import com.pix_service.application.wallet.command.AddPixKeyCommand;
import com.pix_service.domain.gateway.WalletGateway;
import com.pix_service.domain.model.Wallet;
import com.pix_service.shared.application.CommandHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Slf4j
public class AddPixKeyHandler implements CommandHandler<AddPixKeyCommand, Void> {
    private final WalletGateway gateway;

    public AddPixKeyHandler(WalletGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    public Void handle(AddPixKeyCommand command) {
        UUID walletId = command.walletId();
        String pixKey = command.pixKey();

        log.info("Adding pixKey {} to wallet {}", pixKey, walletId);
        if (gateway.findByPixKey(pixKey).isPresent()) {
            log.error("Pix key {} already exists", pixKey);
            throw new IllegalArgumentException("Pix key already registered");
        }

        Wallet wallet = gateway.findById(walletId).orElseThrow();
        Wallet updatedWallet = new Wallet(wallet.getId(), pixKey, wallet.getCurrentBalance());
        gateway.save(updatedWallet);
        log.info("Pix key {} added to wallet {}", pixKey, walletId);
        return null;
    }
}
