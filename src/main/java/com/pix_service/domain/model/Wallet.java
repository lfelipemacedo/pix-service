package com.pix_service.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public class Wallet {
    private UUID id;
    private String pixKey;
    private BigDecimal currentBalance;
    private Long version;

    public Wallet(UUID id) {
        this.id = id;
    }

    public Wallet(UUID id, BigDecimal currentBalance) {
        this.id = id;
        this.currentBalance = currentBalance;
    }

    public Wallet(UUID id, String pixKey, BigDecimal initialBalance) {
        this.id = id;
        this.pixKey = pixKey;
        this.currentBalance = initialBalance;
    }

    public void debit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Valor deve ser positivo");
        if (this.currentBalance.compareTo(amount) < 0) throw new IllegalStateException("Saldo insuficiente");
        this.currentBalance = this.currentBalance.subtract(amount);
    }

    public void credit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("Valor deve ser positivo");
        this.currentBalance = this.currentBalance.add(amount);
    }

    public UUID getId() {
        return id;
    }

    public String getPixKey() {
        return pixKey;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public Long getVersion() {
        return version;
    }
}
