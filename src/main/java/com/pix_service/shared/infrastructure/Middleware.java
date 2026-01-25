package com.pix_service.shared.infrastructure;

import com.pix_service.shared.application.Command;

public interface Middleware {
    <R> R process(Command<R> command, Next<R> next);

    @FunctionalInterface
    interface Next<R> {
        R call();
    }
}
