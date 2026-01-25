package com.pix_service.infrastructure.bus.middleware;

import com.pix_service.shared.application.Command;
import com.pix_service.shared.infrastructure.Middleware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class LoggingMiddleware implements Middleware {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingMiddleware.class);

    @Override
    public <R> R process(Command<R> command, Next<R> next) {
        String commandName = command.getClass().getSimpleName();
        LOGGER.info("[START] Executing command: {}", commandName);
        long start = System.currentTimeMillis();

        try {
            R result = next.call();

            long duration = System.currentTimeMillis() - start;
            LOGGER.info("[FINISH] Command {} finished in {} ms", commandName, duration);
            return result;
        } catch (Exception e) {
            LOGGER.error("[ERROR] Command {} failed: {}", commandName, e.getMessage());
            throw e;
        }
    }
}
