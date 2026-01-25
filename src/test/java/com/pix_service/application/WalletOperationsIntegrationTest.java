package com.pix_service.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pix_service.infrastructure.controller.dto.PixKeyRequest;
import com.pix_service.infrastructure.controller.dto.WalletOperationsRequest;
import com.pix_service.infrastructure.persistence.entity.WalletEntity;
import com.pix_service.infrastructure.persistence.repository.LedgerEntryJpaRepository;
import com.pix_service.infrastructure.persistence.repository.WalletJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class WalletOperationsIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WalletJpaRepository walletRepo;
    @Autowired
    private LedgerEntryJpaRepository ledgerRepo;
    @Autowired
    private ObjectMapper objectMapper;

    private UUID walletId;

    @BeforeEach
    void setup() {
        ledgerRepo.deleteAll();
        walletRepo.deleteAll();

        walletId = UUID.randomUUID();
        WalletEntity wallet = new WalletEntity();
        wallet.setId(walletId);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setPixKey(null);
        walletRepo.saveAndFlush(wallet);
    }

    @Test
    void shouldRegisterPixKey() throws Exception {
        PixKeyRequest pixKeyRequest = PixKeyRequest.with("test@pix.com");

        mockMvc.perform(post("/wallets/" + walletId + "/pix-keys")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pixKeyRequest)))
                .andExpect(status().isOk());

        WalletEntity updated = walletRepo.findById(walletId).get();
        assertEquals("test@pix.com", updated.getPixKey());
    }

    @Test
    void shouldPerformDepositAndWithdrawal() throws Exception {
        WalletOperationsRequest deposit = WalletOperationsRequest.with(new BigDecimal("500.00"));

        mockMvc.perform(post("/wallets/" + walletId + "/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(deposit)))
                .andExpect(status().isNoContent());

        WalletOperationsRequest withdraw = WalletOperationsRequest.with(new BigDecimal("200.00"));

        mockMvc.perform(post("/wallets/" + walletId + "/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withdraw)))
                .andExpect(status().isNoContent());

        WalletEntity updated = walletRepo.findById(walletId).get();
        assertEquals(0, updated.getBalance().compareTo(new BigDecimal("300.00")));
    }

    @Test
    void shouldFailWithdrawalWhenBalanceIsInsufficient() throws Exception {
        WalletOperationsRequest walletOperationsRequest = WalletOperationsRequest.with(new BigDecimal("100"));
        mockMvc.perform(post("/wallets/" + walletId + "/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(walletOperationsRequest)))
                .andExpect(status().isUnprocessableContent());
    }
}
