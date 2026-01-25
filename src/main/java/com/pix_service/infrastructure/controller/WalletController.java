package com.pix_service.infrastructure.controller;

import com.pix_service.application.wallet.command.AddPixKeyCommand;
import com.pix_service.application.wallet.command.CreateWalletCommand;
import com.pix_service.application.wallet.command.GetBalanceCommand;
import com.pix_service.application.wallet.command.WalletOperationsCommand;
import com.pix_service.infrastructure.bus.PipelineCommandBus;
import com.pix_service.infrastructure.controller.dto.CreateWalletRequest;
import com.pix_service.infrastructure.controller.dto.PixKeyRequest;
import com.pix_service.infrastructure.controller.dto.WalletOperationsRequest;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class WalletController {
    private final PipelineCommandBus pipelineCommandBus;

    public WalletController(PipelineCommandBus pipelineCommandBus) {
        this.pipelineCommandBus = pipelineCommandBus;
    }

    @PostMapping
    public ResponseEntity<Map<String, UUID>> create(@RequestBody CreateWalletRequest request) {
        log.info("Creating wallet with request: {}", request);
        return ResponseEntity
                .status(201)
                .body(Map.of("walletId", pipelineCommandBus.dispatch(CreateWalletCommand.with(request.pixKey(), request.balance()))));
    }

    @PostMapping("/{id}/pix-keys")
    public ResponseEntity<Void> registerKey(@PathVariable("id") String walletId, @RequestBody PixKeyRequest request) {
        log.info("Registering pix key for wallet {}: {}", walletId, request.pixKey());
        pipelineCommandBus.dispatch(AddPixKeyCommand.with(walletId, request.pixKey()));
        log.info("Registered pix key for wallet {}: {}", walletId, request.pixKey());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<Map<String, BigDecimal>> getBalance(@PathVariable("id") String walletId) {
        log.info("Getting balance for wallet {}", walletId);
        return ResponseEntity.ok(Map.of("balance", pipelineCommandBus.dispatch(GetBalanceCommand.with(walletId))));
    }

    @PostMapping("/{id}/deposit")
    public ResponseEntity<Void> deposit(@PathVariable("id") String walletId, @RequestBody WalletOperationsRequest request) {
        log.info("Depositing to wallet {}: {}", walletId, request.amount());
        pipelineCommandBus.dispatch(WalletOperationsCommand.with(walletId, request.amount(), "DEPOSIT"));
        log.info("Deposited to wallet {}: {}", walletId, request.amount());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<Void> withdraw(@PathVariable("id") String walletId, @RequestBody WalletOperationsRequest request) {
        log.info("Withdrawing from wallet {}: {}", walletId, request.amount());
        pipelineCommandBus.dispatch(WalletOperationsCommand.with(walletId, request.amount(), "WITHDRAW"));
        log.info("Withdrawn from wallet {}: {}", walletId, request.amount());
        return ResponseEntity.noContent().build();
    }
}
