package com.pix_service.application.wallet.command.handler;

import com.pix_service.application.wallet.command.CreateWalletCommand;
import com.pix_service.domain.gateway.WalletGateway;
import com.pix_service.domain.model.Wallet;
import com.pix_service.shared.application.CommandHandler;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
public class CreateWalletHandler implements CommandHandler<CreateWalletCommand, UUID> {
    private final WalletGateway walletGateway;

    public CreateWalletHandler(WalletGateway walletGateway) {
        this.walletGateway = walletGateway;
    }

    @Override
    public UUID handle(CreateWalletCommand command) {
        String pixKey = command.pixKey();
        BigDecimal initialBalance = command.initialBalance();

        log.info("Creating wallet with pixKey: {} and initialBalance: {}", pixKey, initialBalance);
        if (walletGateway.findByPixKey(pixKey).isPresent()) {
            log.error("Pix key {} already exists", pixKey);
            throw new IllegalArgumentException("Pix key already registered");
        }

        Wallet wallet = new Wallet(UUID.randomUUID(), pixKey, initialBalance);
        log.info("Wallet created with id: {}", wallet.getId());
        return walletGateway.save(wallet).getId();
    }
}
