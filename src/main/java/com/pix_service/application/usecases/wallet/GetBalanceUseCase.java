package com.pix_service.application.usecases.wallet;

import com.pix_service.domain.gateway.WalletGateway;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class GetBalanceUseCase {
    private final WalletGateway walletGateway;

    public GetBalanceUseCase(WalletGateway walletGateway) {
        this.walletGateway = walletGateway;
    }

    public BigDecimal execute(UUID walletId) {
        return walletGateway.findById(walletId).get().getCurrentBalance();
    }
}
