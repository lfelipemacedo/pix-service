package com.pix_service.application.pix.command.handler;

import com.pix_service.application.pix.command.ProcessWebhookCommand;
import com.pix_service.domain.gateway.IdempotencyGateway;
import com.pix_service.domain.gateway.LedgerEntryGateway;
import com.pix_service.domain.gateway.TransactionGateway;
import com.pix_service.domain.gateway.WalletGateway;
import com.pix_service.domain.model.LedgerEntry;
import com.pix_service.domain.model.Transaction;
import com.pix_service.domain.model.TransactionType;
import com.pix_service.domain.model.Wallet;
import com.pix_service.shared.application.CommandHandler;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.UUID;

@Slf4j
public class ProcessWebhookHandler implements CommandHandler<ProcessWebhookCommand, Void> {
    private final TransactionGateway transactionGateway;
    private final WalletGateway walletGateway;
    private final LedgerEntryGateway ledgerGateway;
    private final IdempotencyGateway idempotencyGateway;

    public ProcessWebhookHandler(TransactionGateway transactionGateway, WalletGateway walletGateway, LedgerEntryGateway ledgerGateway, IdempotencyGateway idempotencyGateway) {
        this.transactionGateway = transactionGateway;
        this.walletGateway = walletGateway;
        this.ledgerGateway = ledgerGateway;
        this.idempotencyGateway = idempotencyGateway;
    }

    @Override
    public Void handle(ProcessWebhookCommand command) {
        UUID endToEndId = command.endToEndId();
        UUID eventId = command.eventId();
        String status = command.status();

        log.info("Processing webhook for eventId: {}, endToEndId: {}, status: {}", eventId, endToEndId, status);
        if (idempotencyGateway.isEventProcessed(eventId)) {
            log.info("Event {} already processed, skipping", eventId);
            return null;
        }

        Transaction tx = transactionGateway.findByEndToEndId(endToEndId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        if (tx.isFinalState()) {
            log.info("Transaction {} already in final state, skipping", tx.getId());
            idempotencyGateway.markEventProcessed(eventId);
            log.info("Webhook processing completed for eventId: {}", eventId);
            return null;
        }

        if ("CONFIRMED".equals(status)) {
            log.info("Processing confirmation for transaction {}", tx.getId());
            processConfirmation(tx);
        } else if ("REJECTED".equals(status)) {
            log.info("Processing rejection for transaction {}", tx.getId());
            processRejection(tx);
        }

        transactionGateway.save(tx);
        idempotencyGateway.markEventProcessed(eventId);
        log.info("Webhook processing completed for eventId: {}", eventId);
        return null;
    }

    private void processConfirmation(Transaction transaction) {
        Wallet receiver = walletGateway.findById(transaction.getReceiverWalletId())
                .orElseThrow(() -> new IllegalStateException("Receiver wallet missing"));

        receiver.credit(transaction.getAmount());
        walletGateway.save(receiver);

        ledgerGateway.save(new LedgerEntry(
                UUID.randomUUID(),
                receiver.getId(),
                transaction.getAmount(),
                TransactionType.TRANSFER_IN,
                transaction.getEndToEndId(),
                Instant.now()
        ));

        transaction.confirm();
    }

    private void processRejection(Transaction transaction) {
        Wallet sender = walletGateway.findById(transaction.getSenderWalletId())
                .orElseThrow(() -> new IllegalStateException("Sender wallet missing"));

        sender.credit(transaction.getAmount());
        walletGateway.save(sender);

        ledgerGateway.save(new LedgerEntry(
                UUID.randomUUID(),
                sender.getId(),
                transaction.getAmount(),
                TransactionType.REFUND,
                transaction.getEndToEndId(),
                Instant.now()
        ));

        transaction.reject();
    }
}
