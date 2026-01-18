package com.pix_service.application.usecases.pix;

import com.pix_service.application.dto.WebhookRequest;
import com.pix_service.domain.gateway.IdempotencyGateway;
import com.pix_service.domain.gateway.LedgerEntryGateway;
import com.pix_service.domain.gateway.TransactionGateway;
import com.pix_service.domain.gateway.WalletGateway;
import com.pix_service.domain.model.LedgerEntry;
import com.pix_service.domain.model.Transaction;
import com.pix_service.domain.model.TransactionType;
import com.pix_service.domain.model.Wallet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class ProcessWebhookUseCase {
    private final TransactionGateway transactionGateway;
    private final WalletGateway walletGateway;
    private final LedgerEntryGateway ledgerGateway;
    private final IdempotencyGateway idempotencyGateway;

    public ProcessWebhookUseCase(TransactionGateway transactionGateway, WalletGateway walletGateway, LedgerEntryGateway ledgerGateway, IdempotencyGateway idempotencyGateway) {
        this.transactionGateway = transactionGateway;
        this.walletGateway = walletGateway;
        this.ledgerGateway = ledgerGateway;
        this.idempotencyGateway = idempotencyGateway;
    }

    @Transactional
    public void execute(WebhookRequest webhookRequest) {
        if (idempotencyGateway.isEventProcessed(webhookRequest.eventId())) {
            return;
        }

        Transaction tx = transactionGateway.findByEndToEndId(webhookRequest.endToEndId())
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        if (tx.isFinalState()) {
            idempotencyGateway.markEventProcessed(webhookRequest.eventId());
            return;
        }

        if ("CONFIRMED".equals(webhookRequest.status())) {
            processConfirmation(tx);
        } else if ("REJECTED".equals(webhookRequest.status())) {
            processRejection(tx);
        }

        transactionGateway.save(tx);
        idempotencyGateway.markEventProcessed(webhookRequest.eventId());
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
