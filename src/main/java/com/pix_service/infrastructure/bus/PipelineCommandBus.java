package com.pix_service.infrastructure.bus;

import com.pix_service.shared.infrastructure.Middleware;
import com.pix_service.shared.application.Command;
import com.pix_service.shared.infrastructure.CommandBus;
import com.pix_service.shared.application.CommandHandler;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Primary
public class PipelineCommandBus implements CommandBus {
    private final HandlerRegistry registry;
    private final List<Middleware> middlewares;

    public PipelineCommandBus(HandlerRegistry registry, List<Middleware> middlewares) {
        this.registry = registry;
        this.middlewares = middlewares;
    }

    @Override
    public <R> R dispatch(Command<R> command) {
        CommandHandler<Command<R>, R> handler = registry.get(command.getClass());
        Middleware.Next<R> executionChain = () -> handler.handle(command);

        for (int i = middlewares.size() - 1; i >= 0; i--) {
            Middleware currentMiddleware = middlewares.get(i);
            Middleware.Next<R> next = executionChain;

            executionChain = () -> currentMiddleware.process(command, next);
        }

        return executionChain.call();
    }
}
