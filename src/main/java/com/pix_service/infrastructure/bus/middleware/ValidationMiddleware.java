package com.pix_service.infrastructure.bus.middleware;

import com.pix_service.shared.application.Command;
import com.pix_service.shared.infrastructure.Middleware;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.validation.Validator;

@Component
@Order(2)
public class ValidationMiddleware implements Middleware {
    private final Validator validator;

    public ValidationMiddleware(Validator validator) {
        this.validator = validator;
    }

    @Override
    public <R> R process(Command<R> command, Next<R> next) {
        var violations = validator.validateObject(command);

        if (violations.hasErrors()) {
//            throw new ConstraintViolationException();
        }
        return next.call();
    }
}
