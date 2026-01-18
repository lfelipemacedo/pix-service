package com.pix_service.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public class Transaction {
    private final UUID id;
    private final String endToEndId;
    private final UUID senderWalletId;
    private final UUID receiverWalletId;
    private final BigDecimal amount;
    private TransactionStatus status;

    public Transaction(UUID id, String endToEndId, UUID senderWalletId, UUID receiverWalletId, BigDecimal amount, TransactionStatus status) {
        this.id = id;
        this.endToEndId = endToEndId;
        this.senderWalletId = senderWalletId;
        this.receiverWalletId = receiverWalletId;
        this.amount = amount;
        this.status = status;
    }

    public boolean isFinalState() {
        return status == TransactionStatus.CONFIRMED || status == TransactionStatus.REJECTED;
    }

    public void confirm() {
        if (isFinalState()) return;
        this.status = TransactionStatus.CONFIRMED;
    }

    public void reject() {
        if (isFinalState()) return;
        this.status = TransactionStatus.REJECTED;
    }

    public UUID getId() {
        return id;
    }

    public String getEndToEndId() {
        return endToEndId;
    }

    public UUID getSenderWalletId() {
        return senderWalletId;
    }

    public UUID getReceiverWalletId() {
        return receiverWalletId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public TransactionStatus getStatus() {
        return status;
    }
}
