package com.pix_service.infrastructure.configuration;

import com.pix_service.application.pix.command.handler.ProcessWebhookHandler;
import com.pix_service.application.pix.command.handler.TransferPixHandler;
import com.pix_service.domain.gateway.IdempotencyGateway;
import com.pix_service.domain.gateway.LedgerEntryGateway;
import com.pix_service.domain.gateway.TransactionGateway;
import com.pix_service.domain.gateway.WalletGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PixBeanConfiguration {
    private final TransactionGateway transactionGateway;
    private final WalletGateway walletGateway;
    private final LedgerEntryGateway ledgerGateway;
    private final IdempotencyGateway idempotencyGateway;

    public PixBeanConfiguration(TransactionGateway transactionGateway, WalletGateway walletGateway, LedgerEntryGateway ledgerGateway, IdempotencyGateway idempotencyGateway) {
        this.transactionGateway = transactionGateway;
        this.walletGateway = walletGateway;
        this.ledgerGateway = ledgerGateway;
        this.idempotencyGateway = idempotencyGateway;
    }

    @Bean
    public ProcessWebhookHandler processWebhookHandler() {
        return new ProcessWebhookHandler(transactionGateway, walletGateway, ledgerGateway, idempotencyGateway);
    }

    @Bean
    public TransferPixHandler transferPixHandler() {
        return new TransferPixHandler(transactionGateway, walletGateway, ledgerGateway, idempotencyGateway);
    }
}
