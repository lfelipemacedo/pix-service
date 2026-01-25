package com.pix_service.infrastructure.controller;

import com.pix_service.application.pix.command.ProcessWebhookCommand;
import com.pix_service.application.pix.command.TransferPixCommand;
import com.pix_service.application.pix.dto.TransferPixResponse;
import com.pix_service.infrastructure.bus.PipelineCommandBus;
import com.pix_service.infrastructure.controller.dto.ProcessWebhookRequest;
import com.pix_service.infrastructure.controller.dto.TransferRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/pix")
@Slf4j
public class PixController {
    private final PipelineCommandBus commandBus;

    public PixController(PipelineCommandBus commandBus) {
        this.commandBus = commandBus;
    }

    @PostMapping("/transfers")
    public ResponseEntity<TransferPixResponse> transfer(
            @RequestHeader("Idempotency-Key") UUID idempotencyKey,
            @RequestBody TransferRequest request) {
        log.info("Received transfer request with idempotency key {}: {}", idempotencyKey, request);
        return ResponseEntity.ok(commandBus.dispatch(TransferPixCommand.with(idempotencyKey, request.senderId(), request.pixKey(), request.amount())));
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestBody ProcessWebhookRequest request) {
        log.info("Received webhook request: {}", request);
        commandBus.dispatch(ProcessWebhookCommand.with(request.endToEndId(), request.eventId(), request.status()));
        log.info("Processed webhook request: {}", request);
        return ResponseEntity.ok().build();
    }
}
