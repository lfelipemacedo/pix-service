package com.pix_service.application.usecases.wallet;

import com.pix_service.domain.gateway.WalletGateway;
import com.pix_service.domain.model.Wallet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
public class AddPixKeyUseCase {
    private final WalletGateway gateway;

    public AddPixKeyUseCase(WalletGateway gateway) {
        this.gateway = gateway;
    }

    @Transactional
    public void execute(UUID id, String pixKey) {
        log.info("Adding pixKey {} to wallet {}", pixKey, id);
        if (gateway.findByPixKey(pixKey).isPresent()) {
            log.error("Pix key {} already exists", pixKey);
            throw new IllegalArgumentException("Pix key already registered");
        }

        Wallet wallet = gateway.findById(id).orElseThrow();
        Wallet updatedWallet = new Wallet(wallet.getId(), pixKey, wallet.getCurrentBalance());
        gateway.save(updatedWallet);
        log.info("Pix key {} added to wallet {}", pixKey, id);
    }
}
