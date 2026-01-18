package com.pix_service.application.usecases.pix;

import com.pix_service.application.dto.TransferRequest;
import com.pix_service.application.dto.TransferResponse;
import com.pix_service.domain.gateway.IdempotencyGateway;
import com.pix_service.domain.gateway.LedgerEntryGateway;
import com.pix_service.domain.gateway.TransactionGateway;
import com.pix_service.domain.gateway.WalletGateway;
import com.pix_service.domain.model.LedgerEntry;
import com.pix_service.domain.model.Transaction;
import com.pix_service.domain.model.TransactionStatus;
import com.pix_service.domain.model.TransactionType;
import com.pix_service.domain.model.Wallet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class TransferPixUseCase {
    private final WalletGateway walletGateway;
    private final TransactionGateway transactionGateway;
    private final LedgerEntryGateway ledgerEntryGateway;
    private final IdempotencyGateway idempotencyGateway;

    public TransferPixUseCase(WalletGateway walletGateway, TransactionGateway transactionGateway, LedgerEntryGateway ledgerEntryGateway, IdempotencyGateway idempotencyGateway) {
        this.walletGateway = walletGateway;
        this.transactionGateway = transactionGateway;
        this.ledgerEntryGateway = ledgerEntryGateway;
        this.idempotencyGateway = idempotencyGateway;
    }

    @Transactional
    public TransferResponse execute(String idempotencyKey, TransferRequest transferRequest) {
        var cached = idempotencyGateway.findResult(idempotencyKey);
        if (cached.isPresent()) {
            return (TransferResponse) cached.get();
        }

        Wallet sender = walletGateway.findById(transferRequest.senderId())
                .orElseThrow(() -> new IllegalArgumentException("Sender wallet not found"));

        Wallet receiver = walletGateway.findByPixKey(transferRequest.pixKey())
                .orElseThrow(() -> new IllegalArgumentException("Receiver key not found"));

        if (sender.getId().equals(receiver.getId())) {
            throw new IllegalArgumentException("Cannot transfer to self");
        }

        sender.debit(transferRequest.amount());
        walletGateway.save(sender);

        String endToEndId = UUID.randomUUID().toString();
        UUID transactionId = UUID.randomUUID();

        Transaction tx = new Transaction(transactionId, endToEndId, sender.getId(), receiver.getId(),
                transferRequest.amount(), TransactionStatus.PENDING);
        transactionGateway.save(tx);

        ledgerEntryGateway.save(new LedgerEntry(
                UUID.randomUUID(),
                sender.getId(),
                transferRequest.amount().negate(),
                TransactionType.TRANSFER_OUT,
                endToEndId,
                Instant.now()
        ));

        TransferResponse response = new TransferResponse(endToEndId, "PENDING");
        idempotencyGateway.saveResult(idempotencyKey, response, 86400);

        return response;
    }
}
