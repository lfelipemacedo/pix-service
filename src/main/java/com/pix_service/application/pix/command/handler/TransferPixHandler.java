package com.pix_service.application.pix.command.handler;

import com.pix_service.application.pix.command.TransferPixCommand;
import com.pix_service.application.pix.dto.TransferPixResponse;
import com.pix_service.domain.gateway.IdempotencyGateway;
import com.pix_service.domain.gateway.LedgerEntryGateway;
import com.pix_service.domain.gateway.TransactionGateway;
import com.pix_service.domain.gateway.WalletGateway;
import com.pix_service.domain.model.LedgerEntry;
import com.pix_service.domain.model.Transaction;
import com.pix_service.domain.model.TransactionStatus;
import com.pix_service.domain.model.TransactionType;
import com.pix_service.domain.model.Wallet;
import com.pix_service.shared.application.CommandHandler;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Slf4j
public class TransferPixHandler implements CommandHandler<TransferPixCommand, TransferPixResponse> {
    private final WalletGateway walletGateway;
    private final TransactionGateway transactionGateway;
    private final LedgerEntryGateway ledgerEntryGateway;
    private final IdempotencyGateway idempotencyGateway;

    public TransferPixHandler(TransactionGateway transactionGateway, WalletGateway walletGateway, LedgerEntryGateway ledgerEntryGateway, IdempotencyGateway idempotencyGateway) {
        this.walletGateway = walletGateway;
        this.transactionGateway = transactionGateway;
        this.ledgerEntryGateway = ledgerEntryGateway;
        this.idempotencyGateway = idempotencyGateway;
    }

    @Override
    public TransferPixResponse handle(TransferPixCommand command) {
        UUID idempotencyKey = command.idempotencyKey();
        UUID senderId = command.senderId();
        String pixKey = command.pixKey();
        BigDecimal amount = command.amount();

        log.info("Initiating transfer from wallet {} to pixKey {} amount {}", senderId, pixKey, amount);

        var cached = idempotencyGateway.findResult(idempotencyKey);
        if (cached.isPresent()) {
            log.info("Idempotent transfer detected for key {}, returning cached result", idempotencyKey);
            return (TransferPixResponse) cached.get();
        }

        Wallet sender = walletGateway.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Sender wallet not found"));

        Wallet receiver = walletGateway.findByPixKey(pixKey)
                .orElseThrow(() -> new IllegalArgumentException("Receiver key not found"));

        if (sender.getId().equals(receiver.getId())) {
            log.error("Attempted transfer to self for wallet {}", sender.getId());
            throw new IllegalArgumentException("Cannot transfer to self");
        }

        sender.debit(amount);
        walletGateway.save(sender);
        log.info("Debited amount {} from sender wallet {}", amount, sender.getId());

        UUID endToEndId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();

        Transaction tx = new Transaction(transactionId, endToEndId, sender.getId(), receiver.getId(),
                amount, TransactionStatus.PENDING);
        transactionGateway.save(tx);
        log.info("Created transaction {} with status PENDING", transactionId);

        ledgerEntryGateway.save(new LedgerEntry(
                UUID.randomUUID(),
                sender.getId(),
                amount.negate(),
                TransactionType.TRANSFER_OUT,
                endToEndId,
                Instant.now()
        ));
        log.info("Created ledger entry for sender wallet {}", sender.getId());

        TransferPixResponse response = new TransferPixResponse(endToEndId, "PENDING");
        idempotencyGateway.saveResult(idempotencyKey, response);
        log.info("Transfer process completed for transaction {}", transactionId);

        return response;
    }
}
