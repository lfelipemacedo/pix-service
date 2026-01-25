package com.pix_service.infrastructure.persistence.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pix_service.application.pix.dto.TransferPixResponse;
import com.pix_service.domain.gateway.IdempotencyGateway;
import com.pix_service.infrastructure.persistence.entity.IdempotencyKeyEntity;
import com.pix_service.infrastructure.persistence.repository.IdempotencyKeyJpaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

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
    public Optional<Object> findResult(UUID key) {
        log.info("Searching for idempotency key: {}", key);
        return repository.findById(key)
                .map(entity -> {
                    try {
                        if (entity.getResponseBody() == null) return null;
                        return objectMapper.readValue(entity.getResponseBody(), TransferPixResponse.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Error deserializing idempotency result", e);
                    }
                });
    }

    @Override
    public void saveResult(UUID key, TransferPixResponse result) {
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
    public boolean isEventProcessed(UUID idempotencyKey) {
        log.info("Checking if event {} has been processed", idempotencyKey);
        return repository.existsById(idempotencyKey);
    }

    @Override
    public void markEventProcessed(UUID idempotencyKey) {
        log.info("Marking event {} as processed", idempotencyKey);
        IdempotencyKeyEntity entity = new IdempotencyKeyEntity(idempotencyKey, null, Instant.now());
        repository.save(entity);
    }
}
