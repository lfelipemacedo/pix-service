package com.pix_service.application.pix.command;

import com.pix_service.shared.application.Command;

import java.util.UUID;

public record ProcessWebhookCommand(
        UUID endToEndId,
        UUID eventId,
        String status
) implements Command<Void> {
    public static ProcessWebhookCommand with(UUID endToEndId, UUID eventId, String status) {
        return new ProcessWebhookCommand(endToEndId, eventId, status);
    }
}
