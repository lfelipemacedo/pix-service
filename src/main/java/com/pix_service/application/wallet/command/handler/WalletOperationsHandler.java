package com.pix_service.application.wallet.command.handler;

import com.pix_service.application.wallet.command.WalletOperationsCommand;
import com.pix_service.domain.gateway.WalletGateway;
import com.pix_service.domain.model.Wallet;
import com.pix_service.shared.application.CommandHandler;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
public class WalletOperationsHandler implements CommandHandler<WalletOperationsCommand, Void> {
    private final WalletGateway gateway;

    public WalletOperationsHandler(WalletGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    public Void handle(WalletOperationsCommand command) {
        UUID id = command.id();
        BigDecimal amount = command.amount();
        String type = command.type();

        if ("DEPOSIT".equals(type)) {
            deposit(id, amount);
            return null;
        }
        withdraw(id, amount);
        return null;
    }

    private void deposit(UUID id, BigDecimal amount) {
        log.info("Depositing amount {} to wallet {}", amount, id);
        Wallet wallet = gateway.findById(id).orElseThrow();
        wallet.credit(amount);
        gateway.save(wallet);
        gateway.recordLedger(id, amount, "DEPOSIT", UUID.randomUUID());
        log.info("Deposited amount {} to wallet {}", amount, id);
    }

    private void withdraw(UUID id, BigDecimal amount) {
        log.info("Withdrawing amount {} from wallet {}", amount, id);
        Wallet wallet = gateway.findById(id).orElseThrow();
        wallet.debit(amount);
        gateway.save(wallet);
        gateway.recordLedger(id, amount.negate(), "WITHDRAW", UUID.randomUUID());
        log.info("Withdrew amount {} from wallet {}", amount, id);
    }
}
