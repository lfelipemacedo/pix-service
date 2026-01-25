package com.pix_service.infrastructure.persistence.adapter;

import com.pix_service.domain.gateway.WalletGateway;
import com.pix_service.domain.model.Wallet;
import com.pix_service.infrastructure.persistence.entity.WalletEntity;
import com.pix_service.infrastructure.persistence.repository.WalletJpaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class WalletRepositoryAdapter implements WalletGateway {
    private final WalletJpaRepository repository;
    private final LedgerRepositoryAdapter ledgerRepositoryAdapter;

    public WalletRepositoryAdapter(WalletJpaRepository repository, LedgerRepositoryAdapter ledgerRepositoryAdapter) {
        this.repository = repository;
        this.ledgerRepositoryAdapter = ledgerRepositoryAdapter;
    }

    @Override
    public Optional<Wallet> findById(UUID id) {
        log.info("Searching for wallet with id {}", id);
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Wallet> findByPixKey(String key) {
        log.info("Searching for wallet with pixKey {}", key);
        return repository.findByPixKey(key).map(this::toDomain);
    }

    @Override
    public BigDecimal getBalanceAt(UUID walletId, Instant at) {
        log.info("Getting balance for wallet {} at {}", walletId, at);
        return ledgerRepositoryAdapter.calculateBalanceAt(walletId, at);
    }

    @Override
    public void recordLedger(UUID walletId, BigDecimal amount, String type, UUID endToEndId) {
        log.info("Recording ledger entry for wallet {}: amount {}, type {}, endToEndId {}", walletId, amount, type, endToEndId);
        ledgerRepositoryAdapter.recordLedger(walletId, amount, type, endToEndId);
    }

    @Override
    public Wallet save(Wallet wallet) {
        log.info("Saving wallet with id {}", wallet.getId());
        WalletEntity entity = repository.findById(wallet.getId())
                .orElse(new WalletEntity());

        entity.setId(wallet.getId());
        entity.setBalance(wallet.getCurrentBalance());
        entity.setPixKey(wallet.getPixKey());

        WalletEntity createdWallet = repository.save(entity);
        return new Wallet(createdWallet.getId());
    }

    private Wallet toDomain(WalletEntity entity) {
        return new Wallet(entity.getId(), entity.getPixKey(), entity.getBalance());
    }
}
