package com.pix_service.infrastructure.controller.dto;

public record PixKeyRequest(String pixKey) {
    public static PixKeyRequest with(String pixKey) {
        return new PixKeyRequest(pixKey);
    }
}
