package com.pix_service.application.usecases.wallet;

import com.pix_service.domain.gateway.WalletGateway;
import com.pix_service.domain.model.Wallet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AddPixKeyUseCase {
    private final WalletGateway gateway;

    public AddPixKeyUseCase(WalletGateway gateway) {
        this.gateway = gateway;
    }

    @Transactional
    public void execute(UUID id, String pixKey) {
        if (gateway.findByPixKey(pixKey).isPresent()) throw new RuntimeException("Chave já existe");

        Wallet wallet = gateway.findById(id).orElseThrow();
        Wallet updatedWallet = new Wallet(wallet.getId(), pixKey, wallet.getCurrentBalance());
        gateway.save(updatedWallet);
    }
}
