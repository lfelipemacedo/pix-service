package com.pix_service.infrastructure.persistence.adapter;

import com.pix_service.domain.gateway.TransactionGateway;
import com.pix_service.domain.model.Transaction;
import com.pix_service.domain.model.TransactionStatus;
import com.pix_service.infrastructure.persistence.entity.TransactionEntity;
import com.pix_service.infrastructure.persistence.repository.TransactionJpaRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
public class TransactionRepositoryAdapter implements TransactionGateway {
    private final TransactionJpaRepository repository;

    public TransactionRepositoryAdapter(TransactionJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(Transaction transaction) {
        TransactionEntity entity = toEntity(transaction);
        repository.save(entity);
    }

    @Override
    public Optional<Transaction> findByEndToEndId(String endToEndId) {
        return repository.findByEndToEndId(endToEndId).map(this::toDomain);
    }

    private Transaction toDomain(TransactionEntity entity) {
        return new Transaction(
                entity.getId(),
                entity.getEndToEndId(),
                entity.getSenderWalletId(),
                entity.getReceiverWalletId(),
                entity.getAmount(),
                TransactionStatus.valueOf(entity.getStatus())
        );
    }

    private TransactionEntity toEntity(Transaction domain) {
        TransactionEntity entity = new TransactionEntity();
        entity.setId(domain.getId());
        entity.setEndToEndId(domain.getEndToEndId());
        entity.setSenderWalletId(domain.getSenderWalletId());
        entity.setReceiverWalletId(domain.getReceiverWalletId());
        entity.setAmount(domain.getAmount());
        entity.setStatus(domain.getStatus().name());

        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(Instant.now());
        }
        return entity;
    }
}
