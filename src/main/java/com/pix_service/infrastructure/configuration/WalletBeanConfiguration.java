package com.pix_service.infrastructure.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pix_service.application.wallet.command.handler.AddPixKeyHandler;
import com.pix_service.application.wallet.command.handler.CreateWalletHandler;
import com.pix_service.application.wallet.command.handler.GetBalanceHandler;
import com.pix_service.application.wallet.command.handler.WalletOperationsHandler;
import com.pix_service.domain.gateway.WalletGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WalletBeanConfiguration {

    private final WalletGateway walletGateway;

    public WalletBeanConfiguration(WalletGateway walletGateway) {
        this.walletGateway = walletGateway;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public AddPixKeyHandler addPixKeyHandler() {
        return new AddPixKeyHandler(walletGateway);
    }

    @Bean
    public CreateWalletHandler createWalletHandler() {
        return new CreateWalletHandler(walletGateway);
    }

    @Bean
    public GetBalanceHandler getBalanceHandler() {
        return new GetBalanceHandler(walletGateway);
    }

    @Bean
    public WalletOperationsHandler walletOperationsHandler() {
        return new WalletOperationsHandler(walletGateway);
    }
}
