package com.pix_service.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
public class TransactionEntity {
    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID id;

    @Column(name = "end_to_end_id", nullable = false, unique = true)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID endToEndId;

    @Column(name = "sender_wallet_id", nullable = false)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID senderWalletId;

    @Column(name = "receiver_wallet_id", nullable = false)
    @JdbcTypeCode(SqlTypes.UUID)
    private UUID receiverWalletId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String status;

    @Column(name = "created_at")
    private Instant createdAt;
}
