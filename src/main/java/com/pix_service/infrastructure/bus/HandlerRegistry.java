package com.pix_service.infrastructure.bus;

import com.pix_service.shared.application.Command;
import com.pix_service.shared.application.CommandHandler;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.core.GenericTypeResolver;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class HandlerRegistry {
    private final Map<Class<? extends Command<?>>, CommandHandler<?, ?>> registry = new HashMap<>();

    public HandlerRegistry(List<CommandHandler<?, ?>> handlers) {
        for (CommandHandler<?, ?> handler : handlers) {
            Class<?> targetClass = AopProxyUtils.ultimateTargetClass(handler);
            Class<?>[] generics = GenericTypeResolver.resolveTypeArguments(targetClass, CommandHandler.class);

            if (generics == null || generics.length == 0) {
                continue;
            }

            Class<? extends Command<?>> commandType = (Class<? extends Command<?>>) generics[0];

            registry.put(commandType, handler);
        }
    }

    public <C extends Command<R>, R> CommandHandler<?, ?> get(Class<C> commandClass) {
        return registry.get(commandClass);
    }
}
