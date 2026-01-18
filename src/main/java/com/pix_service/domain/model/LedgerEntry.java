package com.pix_service.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;


public record LedgerEntry(UUID id,
                          UUID walletId,
                          BigDecimal amount,
                          TransactionType transactionType,
                          String endToEndId,
                          Instant createdAt) {
}
