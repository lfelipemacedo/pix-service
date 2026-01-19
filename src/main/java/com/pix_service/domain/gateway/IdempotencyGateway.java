package com.pix_service.domain.gateway;

import java.util.Optional;

public interface IdempotencyGateway {
    Optional<Object> findResult(String key);

    void saveResult(String key, Object result);

    boolean isEventProcessed(String eventId);

    void markEventProcessed(String eventId);
}
