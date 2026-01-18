package com.pix_service.infrastructure.controller;

import com.pix_service.application.usecases.wallet.AddPixKeyUseCase;
import com.pix_service.application.usecases.wallet.CreateWalletUseCase;
import com.pix_service.application.usecases.wallet.GetBalanceUseCase;
import com.pix_service.application.usecases.wallet.WalletOperationsUseCase;
import com.pix_service.domain.model.Wallet;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/wallets")
public class WalletController {
    private final CreateWalletUseCase createUseCase;
    private final GetBalanceUseCase getBalanceUseCase;
    private final AddPixKeyUseCase addPixKeyUseCase;
    private final WalletOperationsUseCase walletOperationsUseCase;

    public WalletController(CreateWalletUseCase createUseCase, GetBalanceUseCase getBalanceUseCase, AddPixKeyUseCase addPixKeyUseCase, WalletOperationsUseCase walletOperationsUseCase) {
        this.createUseCase = createUseCase;
        this.getBalanceUseCase = getBalanceUseCase;
        this.addPixKeyUseCase = addPixKeyUseCase;
        this.walletOperationsUseCase = walletOperationsUseCase;
    }

    @PostMapping
    public ResponseEntity<Map<String, UUID>> create(@RequestBody Map<String, Object> request) {
        Wallet createdWallet = createUseCase.execute((String) request.get("pixKey"), new BigDecimal(request.get("balance").toString()));
        return ResponseEntity.status(201).body(Map.of("id", createdWallet.getId()));
    }

    @PostMapping("/{id}/pix-keys")
    public ResponseEntity<Void> registerKey(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        addPixKeyUseCase.execute(id, body.get("pixKey"));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<Map<String, BigDecimal>> getBalance(@PathVariable UUID id) {
        BigDecimal balance = getBalanceUseCase.execute(id);
        return ResponseEntity.ok(Map.of("balance", balance));
    }

    @PostMapping("/{id}/deposit")
    public ResponseEntity<Void> deposit(@PathVariable UUID id, @RequestBody Map<String, BigDecimal> body) {
        walletOperationsUseCase.deposit(id, body.get("amount"));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<Void> withdraw(@PathVariable UUID id, @RequestBody Map<String, BigDecimal> body) {
        walletOperationsUseCase.withdraw(id, body.get("amount"));
        return ResponseEntity.noContent().build();
    }
}
