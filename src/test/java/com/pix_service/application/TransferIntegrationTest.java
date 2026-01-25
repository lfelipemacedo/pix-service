package com.pix_service.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pix_service.infrastructure.controller.dto.TransferRequest;
import com.pix_service.infrastructure.persistence.entity.WalletEntity;
import com.pix_service.infrastructure.persistence.repository.IdempotencyKeyJpaRepository;
import com.pix_service.infrastructure.persistence.repository.LedgerEntryJpaRepository;
import com.pix_service.infrastructure.persistence.repository.TransactionJpaRepository;
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

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TransferIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WalletJpaRepository walletRepo;
    @Autowired
    private IdempotencyKeyJpaRepository idempotencyRepo;
    @Autowired
    private TransactionJpaRepository transactionRepo;
    @Autowired
    private LedgerEntryJpaRepository ledgerRepo;
    @Autowired
    private ObjectMapper objectMapper;

    private UUID senderId;
    private final String receiverPixKey = "felipe@pix.com";

    @BeforeEach
    void setup() {
        ledgerRepo.deleteAll();
        transactionRepo.deleteAll();
        idempotencyRepo.deleteAll();
        walletRepo.deleteAll();
        walletRepo.flush();

        senderId = UUID.randomUUID();
        WalletEntity sender = new WalletEntity();
        sender.setId(senderId);
        sender.setBalance(new BigDecimal("1000.00"));
        sender.setPixKey("bob@pix.com");
        sender.setVersion(null);
        walletRepo.saveAndFlush(sender);

        WalletEntity receiver = new WalletEntity();
        receiver.setId(UUID.randomUUID());
        receiver.setBalance(new BigDecimal("0.00"));
        receiver.setPixKey(receiverPixKey);
        receiver.setVersion(null);
        walletRepo.saveAndFlush(receiver);
    }

    @Test
    void shouldProcessTransferAndDeductBalance() throws Exception {
        TransferRequest req = new TransferRequest(senderId, receiverPixKey, new BigDecimal("100.00"));
        String idempotencyKey = UUID.randomUUID().toString();

        mockMvc.perform(post("/pix/transfers")
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("PENDING")));

        WalletEntity updatedSender = walletRepo.findById(senderId).get();
        assertEquals(0, updatedSender.getBalance().compareTo(new BigDecimal("900.00")),
                "O saldo deveria ser 900.00");
    }

    @Test
    void shouldReturnCachedResponseForSameIdempotencyKey() throws Exception {
        TransferRequest req = new TransferRequest(senderId, receiverPixKey, new BigDecimal("100.00"));
        UUID key = UUID.randomUUID();

        mockMvc.perform(post("/pix/transfers")
                        .header("Idempotency-Key", key)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        System.out.println("Chaves salvas: " + idempotencyRepo.findAll().size());
        idempotencyRepo.findAll().forEach(k -> System.out.println("Key: " + k.getKeyId() + " Body: " + k.getResponseBody()));

        mockMvc.perform(post("/pix/transfers")
                        .header("Idempotency-Key", key)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        WalletEntity sender = walletRepo.findById(senderId).get();
        assertEquals(0, sender.getBalance().compareTo(new BigDecimal("900.00")),
                "O saldo não deveria mudar na segunda chamada idempotente");
    }
}
