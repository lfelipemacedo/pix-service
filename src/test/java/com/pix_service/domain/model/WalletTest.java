package com.pix_service.domain.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class WalletTest {
    @Test
    void shouldDebitSuccessfullyWhenBalanceIsSufficient() {
        Wallet wallet = new Wallet(UUID.randomUUID(), new BigDecimal("100.00"));
        wallet.debit(new BigDecimal("40.00"));

        assertEquals(new BigDecimal("60.00"), wallet.getCurrentBalance());
    }

    @Test
    void shouldThrowExceptionWhenBalanceIsInsufficient() {
        Wallet wallet = new Wallet(UUID.randomUUID(), new BigDecimal("50.00"));

        assertThrows(IllegalStateException.class, () -> {
            wallet.debit(new BigDecimal("50.01"));
        });
    }

    @Test
    void shouldThrowExceptionForNegativeAmount() {
        Wallet wallet = new Wallet(UUID.randomUUID(), new BigDecimal("100.00"));

        assertThrows(IllegalArgumentException.class, () -> {
            wallet.credit(new BigDecimal("-10.00"));
        });
    }
}
