package com.pix_service.infrastructure.controller;

import com.pix_service.application.dto.TransferRequest;
import com.pix_service.application.dto.TransferResponse;
import com.pix_service.application.dto.WebhookRequest;
import com.pix_service.application.usecases.pix.ProcessWebhookUseCase;
import com.pix_service.application.usecases.pix.TransferPixUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pix")
public class PixController {
    private final TransferPixUseCase transferPixUseCase;
    private final ProcessWebhookUseCase processWebhookUseCase;

    public PixController(TransferPixUseCase transferPixUseCase, ProcessWebhookUseCase processWebhookUseCase) {
        this.transferPixUseCase = transferPixUseCase;
        this.processWebhookUseCase = processWebhookUseCase;
    }

    @PostMapping("/transfers")
    public ResponseEntity<TransferResponse> transfer(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody TransferRequest request) {
        return ResponseEntity.ok(transferPixUseCase.execute(idempotencyKey, request));
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestBody WebhookRequest request) {
        processWebhookUseCase.execute(request);
        return ResponseEntity.ok().build();
    }
}
