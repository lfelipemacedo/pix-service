package com.pix_service.infrastructure.persistence.repository;

import com.pix_service.infrastructure.persistence.entity.LedgerEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Repository
public interface LedgerEntryJpaRepository extends JpaRepository<LedgerEntryEntity, UUID> {
    @Query("SELECT COALESCE(SUM(l.amount), 0) FROM LedgerEntryEntity l WHERE l.walletId = :id AND l.createdAt <= :at")
    BigDecimal calculateBalanceAt(@Param("id") UUID id, @Param("at") Instant at);
}
