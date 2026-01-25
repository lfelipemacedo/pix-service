package com.pix_service.infrastructure.bus.middleware;

import com.pix_service.shared.application.Command;
import com.pix_service.shared.infrastructure.Middleware;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(3)
public class TransactionMiddleware implements Middleware {
    @Override
    @Transactional
    public <R> R process(Command<R> command, Next<R> next) {
        return next.call();
    }
}
