package com.pix_service.application.usecases.wallet;

import com.pix_service.domain.gateway.WalletGateway;
import com.pix_service.domain.model.Wallet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class GetBalanceUseCase {
    private final WalletGateway walletGateway;

    public GetBalanceUseCase(WalletGateway walletGateway) {
        this.walletGateway = walletGateway;
    }

    public BigDecimal execute(UUID walletId) {
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
