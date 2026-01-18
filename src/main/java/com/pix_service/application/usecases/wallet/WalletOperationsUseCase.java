package com.pix_service.application.usecases.wallet;

import com.pix_service.domain.gateway.WalletGateway;
import com.pix_service.domain.model.Wallet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class WalletOperationsUseCase {
    private final WalletGateway gateway;

    public WalletOperationsUseCase(WalletGateway gateway) {
        this.gateway = gateway;
    }

    @Transactional
    public void deposit(UUID id, BigDecimal amount) {
        Wallet wallet = gateway.findById(id).orElseThrow();
        wallet.credit(amount);
        gateway.save(wallet);
        gateway.recordLedger(id, amount, "DEPOSIT", "DEP-" + UUID.randomUUID());
    }

    @Transactional
    public void withdraw(UUID id, BigDecimal amount) {
        Wallet wallet = gateway.findById(id).orElseThrow();
        wallet.debit(amount);
        gateway.save(wallet);
        gateway.recordLedger(id, amount.negate(), "WITHDRAW", "WTH-" + UUID.randomUUID());
    }
}
