package com.pix_service.domain.gateway;

import com.pix_service.domain.model.Transaction;

import java.util.Optional;

public interface TransactionGateway {
    void save(Transaction transaction);

    Optional<Transaction> findByEndToEndId(String endToEndId);
}
