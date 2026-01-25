package com.pix_service.application.pix.dto;

import java.util.UUID;

public record TransferPixResponse(UUID endToEndId, String status) {
}
