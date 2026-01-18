package com.pix_service.infrastructure.persistence.adapter;

import com.pix_service.domain.gateway.LedgerEntryGateway;
import com.pix_service.domain.model.LedgerEntry;
import com.pix_service.infrastructure.persistence.entity.LedgerEntryEntity;
import com.pix_service.infrastructure.persistence.repository.LedgerEntryJpaRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Component
public class LedgerRepositoryAdapter implements LedgerEntryGateway {
    private final LedgerEntryJpaRepository repository;

    public LedgerRepositoryAdapter(LedgerEntryJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(LedgerEntry entry) {
        LedgerEntryEntity entity = new LedgerEntryEntity();
        entity.setId(entry.id());
        entity.setWalletId(entry.walletId());
        entity.setAmount(entry.amount());
        entity.setType(entry.transactionType().name());
        entity.setEndToEndId(entry.endToEndId());
        entity.setCreatedAt(entry.createdAt());

        repository.save(entity);
    }

    @Override
    public BigDecimal calculateBalanceAt(UUID id, Instant at) {
        return repository.calculateBalanceAt(id, at);
    }

    @Override
    public void recordLedger(UUID walletId, BigDecimal amount, String type, String endToEndId) {
        LedgerEntryEntity entity = new LedgerEntryEntity();
        entity.setId(UUID.randomUUID());
        entity.setWalletId(walletId);
        entity.setAmount(amount);
        entity.setType(type);
        entity.setEndToEndId(endToEndId);
        entity.setCreatedAt(Instant.now());

        repository.save(entity);
    }
}
