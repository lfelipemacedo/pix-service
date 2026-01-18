package com.pix_service.application.dto;

public record WebhookRequest(String endToEndId, String eventId, String status) {
}
