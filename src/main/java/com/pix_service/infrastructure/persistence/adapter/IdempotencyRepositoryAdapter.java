package com.pix_service.infrastructure.persistence.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pix_service.application.dto.TransferResponse;
import com.pix_service.domain.gateway.IdempotencyGateway;
import com.pix_service.infrastructure.persistence.entity.IdempotencyKeyEntity;
import com.pix_service.infrastructure.persistence.repository.IdempotencyKeyJpaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Component
public class IdempotencyRepositoryAdapter implements IdempotencyGateway {
    private final IdempotencyKeyJpaRepository repository;
    private final ObjectMapper objectMapper;

    public IdempotencyRepositoryAdapter(IdempotencyKeyJpaRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<Object> findResult(String key) {
        log.info("Searching for idempotency key: {}", key);
        return repository.findById(key)
                .map(entity -> {
                    try {
                        if (entity.getResponseBody() == null) return null;
                        return objectMapper.readValue(entity.getResponseBody(), TransferResponse.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Error deserializing idempotency result", e);
                    }
                });
    }

    @Override
    public void saveResult(String key, Object result) {
        log.info("Saving idempotency result for key: {}", key);
        try {
            String json = objectMapper.writeValueAsString(result);
            IdempotencyKeyEntity entity = new IdempotencyKeyEntity(key, json, Instant.now());
            repository.save(entity);
        } catch (JsonProcessingException e) {
            log.error("Error serializing idempotency result for key: {}", key, e);
            throw new IllegalArgumentException("Error serializing idempotency result", e);
        }
    }

    @Override
    public boolean isEventProcessed(String eventId) {
        log.info("Checking if event {} has been processed", eventId);
        return repository.existsById(eventId);
    }

    @Override
    public void markEventProcessed(String eventId) {
        log.info("Marking event {} as processed", eventId);
        IdempotencyKeyEntity entity = new IdempotencyKeyEntity(eventId, null, Instant.now());
        repository.save(entity);
    }
}
