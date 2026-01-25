package com.pix_service.application;

import com.pix_service.application.pix.command.TransferPixCommand;
import com.pix_service.application.pix.command.handler.TransferPixHandler;
import com.pix_service.infrastructure.persistence.entity.WalletEntity;
import com.pix_service.infrastructure.persistence.repository.WalletJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ConcurrencyTest {
    @Autowired
    private TransferPixHandler transferPixCommandHandler;
    @Autowired
    private WalletJpaRepository walletRepo;

    @Test
    void shouldPreventDoubleSpendingWithOptimisticLocking() throws InterruptedException {
        walletRepo.deleteAll();
        walletRepo.flush();

        UUID senderId = UUID.randomUUID();
        WalletEntity sender = new WalletEntity();
        sender.setId(senderId);
        sender.setBalance(new BigDecimal("100.00"));
        sender.setVersion(null);
        sender.setPixKey("concurrency@test.com");
        walletRepo.saveAndFlush(sender);

        WalletEntity receiver = new WalletEntity();
        receiver.setId(UUID.randomUUID());
        receiver.setBalance(BigDecimal.ZERO);
        receiver.setPixKey("receiver@test.com");
        receiver.setVersion(null);
        walletRepo.saveAndFlush(receiver);

        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        List<Future<String>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                try {
                    latch.await();
                    UUID idempotencyKey = UUID.randomUUID();
                    TransferPixCommand transferPixCommand = new TransferPixCommand(idempotencyKey, senderId, "receiver@test.com", new BigDecimal("80.00"));

                    transferPixCommandHandler.handle(transferPixCommand);
                    return "SUCCESS";
                } catch (ObjectOptimisticLockingFailureException e) {
                    return "OPTIMISTIC_LOCK_FAIL";
                } catch (Exception e) {
                    return "OTHER_FAIL: " + e.getMessage();
                }
            }));
        }

        latch.countDown();
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        int successCount = 0;
        int lockFailCount = 0;

        for (Future<String> future : futures) {
            try {
                String result = future.get();
                if ("SUCCESS".equals(result)) successCount++;
                if ("OPTIMISTIC_LOCK_FAIL".equals(result)) lockFailCount++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println("Successos: " + successCount);
        System.out.println("Bloqueios de Concorrência: " + lockFailCount);

        assertEquals(1, successCount, "Apenas uma transferência deveria passar");

        BigDecimal finalBalance = walletRepo.findById(senderId).get().getBalance();
        assertEquals(0, finalBalance.compareTo(new BigDecimal("20.00")), "Saldo final incorreto");
    }
}
