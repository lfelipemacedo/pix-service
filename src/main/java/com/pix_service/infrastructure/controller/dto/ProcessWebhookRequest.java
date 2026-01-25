package com.pix_service.infrastructure.controller.dto;

import java.util.UUID;

public record ProcessWebhookRequest(UUID endToEndId, UUID eventId, String status) {
}
