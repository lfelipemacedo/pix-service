package com.pix_service.domain.gateway;

import com.pix_service.domain.model.Wallet;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface WalletGateway {
    Wallet save(Wallet wallet);
    Optional<Wallet> findById(UUID id);
    Optional<Wallet> findByPixKey(String pixKey);
    BigDecimal getBalanceAt(UUID walletId, Instant at);
    void recordLedger(UUID walletId, BigDecimal amount, String type, String endToEndId);
}
