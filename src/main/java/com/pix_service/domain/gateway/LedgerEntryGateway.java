package com.pix_service.domain.gateway;

import com.pix_service.domain.model.LedgerEntry;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public interface LedgerEntryGateway {
    void save(LedgerEntry entry);

    BigDecimal calculateBalanceAt(UUID id, Instant at);

    void recordLedger(UUID walletId, BigDecimal amount, String type, String endToEndId);
}
