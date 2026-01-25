package com.pix_service.application.wallet.command.handler;

import com.pix_service.application.wallet.command.GetBalanceCommand;
import com.pix_service.domain.gateway.WalletGateway;
import com.pix_service.domain.model.Wallet;
import com.pix_service.shared.application.CommandHandler;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Slf4j
public class GetBalanceHandler implements CommandHandler<GetBalanceCommand, BigDecimal> {
    private final WalletGateway walletGateway;

    public GetBalanceHandler(WalletGateway walletGateway) {
        this.walletGateway = walletGateway;
    }

    @Override
    public BigDecimal handle(GetBalanceCommand command) {
        UUID walletId = command.walletId();

        log.info("Fetching balance for wallet {}", walletId);
        Optional<Wallet> optionalWallet = walletGateway.findById(walletId);
        if (optionalWallet.isEmpty()) {
            log.error("Wallet {} not found", walletId);
            throw new IllegalArgumentException("Wallet not found");
        }
        BigDecimal currentBalance = optionalWallet.get().getCurrentBalance();
        log.info("Balance for wallet {} is {}", walletId, currentBalance);
        return currentBalance;
    }
}
