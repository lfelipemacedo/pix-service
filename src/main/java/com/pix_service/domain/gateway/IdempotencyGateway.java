package com.pix_service.domain.gateway;

import com.pix_service.application.pix.dto.TransferPixResponse;

import java.util.Optional;
import java.util.UUID;

public interface IdempotencyGateway {
    Optional<Object> findResult(UUID key);

    void saveResult(UUID key, TransferPixResponse response);

    boolean isEventProcessed(UUID eventId);

    void markEventProcessed(UUID eventId);
}
