package com.pix_service.application;

import com.pix_service.infrastructure.persistence.entity.LedgerEntryEntity;
import com.pix_service.infrastructure.persistence.repository.LedgerEntryJpaRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class HistoricalBalanceTest {
    @Autowired
    private LedgerEntryJpaRepository ledgerRepo;

    @Test
    void shouldCalculateBalanceAtSpecificTimestamp() {
        ledgerRepo.deleteAll();
        ledgerRepo.flush();

        UUID walletId = UUID.randomUUID();

        Instant pastDate = Instant.parse("2020-01-01T10:00:00Z");
        Instant futureDate = Instant.parse("2020-01-01T12:00:00Z");

        LedgerEntryEntity entry1 = new LedgerEntryEntity();
        entry1.setId(UUID.randomUUID());
        entry1.setWalletId(walletId);
        entry1.setAmount(new BigDecimal("1000.00"));
        entry1.setType("DEPOSIT");
        entry1.setEndToEndId("E2E-" + UUID.randomUUID());
        entry1.setCreatedAt(pastDate);
        ledgerRepo.saveAndFlush(entry1);

        LedgerEntryEntity entry2 = new LedgerEntryEntity();
        entry2.setId(UUID.randomUUID());
        entry2.setWalletId(walletId);
        entry2.setAmount(new BigDecimal("-200.00"));
        entry2.setType("TRANSFER");
        entry2.setEndToEndId("E2E-" + UUID.randomUUID());
        entry2.setCreatedAt(futureDate);
        ledgerRepo.saveAndFlush(entry2);

        BigDecimal balance1 = ledgerRepo.calculateBalanceAt(walletId, pastDate.plusSeconds(1));
        Assertions.assertEquals(0, balance1.compareTo(new BigDecimal("1000.00")),
                "Deveria ser 1000.00, mas veio: " + balance1);

        BigDecimal balance2 = ledgerRepo.calculateBalanceAt(walletId, futureDate.plusSeconds(1));
        Assertions.assertEquals(0, balance2.compareTo(new BigDecimal("800.00")),
                "Deveria ser 800.00, mas veio: " + balance2);
    }
}
