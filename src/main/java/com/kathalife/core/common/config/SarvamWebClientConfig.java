package com.kathalife.core.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class SarvamWebClientConfig {

    private final SarvamProperties sarvamProperties;

    public SarvamWebClientConfig(SarvamProperties sarvamProperties) {
        this.sarvamProperties = sarvamProperties;
    }

    @Bean
    public WebClient sarvamWebClient() {
        return WebClient.builder()
                .baseUrl(sarvamProperties.api().baseUrl())
                .defaultHeader("api-subscription-key", sarvamProperties.api().key())
                .build();
    }
}
