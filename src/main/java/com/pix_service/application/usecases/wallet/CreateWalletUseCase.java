package com.pix_service.application.usecases.wallet;

import com.pix_service.application.exception.PixKeyAlreadyExistException;
import com.pix_service.domain.gateway.WalletGateway;
import com.pix_service.domain.model.Wallet;
import com.pix_service.infrastructure.persistence.entity.WalletEntity;
import com.pix_service.infrastructure.persistence.repository.WalletJpaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Slf4j
public class CreateWalletUseCase {
    private final WalletGateway walletGateway;

    public CreateWalletUseCase(WalletGateway walletGateway) {
        this.walletGateway = walletGateway;
    }

    @Transactional
    public Wallet execute(String pixKey, BigDecimal initialBalance) {
        log.info("Creating wallet with pixKey: {} and initialBalance: {}", pixKey, initialBalance);
        if (walletGateway.findByPixKey(pixKey).isPresent()) {
            log.error("Pix key {} already exists", pixKey);
            throw new IllegalArgumentException("Pix key already registered");
        }

        Wallet wallet = new Wallet(UUID.randomUUID(), pixKey, initialBalance);
        log.info("Wallet created with id: {}", wallet.getId());
        return walletGateway.save(wallet);
    }
}
