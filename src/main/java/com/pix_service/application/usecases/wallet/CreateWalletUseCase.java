package com.pix_service.application.usecases.wallet;

import com.pix_service.domain.gateway.WalletGateway;
import com.pix_service.domain.model.Wallet;
import com.pix_service.infrastructure.persistence.entity.WalletEntity;
import com.pix_service.infrastructure.persistence.repository.WalletJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class CreateWalletUseCase {
    private final WalletGateway walletGateway;

    public CreateWalletUseCase(WalletGateway walletGateway) {
        this.walletGateway = walletGateway;
    }

    @Transactional
    public Wallet execute(String pixKey, BigDecimal initialBalance) {
        if (walletGateway.findByPixKey(pixKey).isPresent()) {
            throw new RuntimeException("Chave Pix já cadastrada");
        }

        Wallet wallet = new Wallet(UUID.randomUUID(), pixKey, initialBalance);
        return walletGateway.save(wallet);
    }
}
