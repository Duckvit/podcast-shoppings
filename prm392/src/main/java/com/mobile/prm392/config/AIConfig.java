package com.mobile.prm392.config;

import com.google.genai.Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIConfig {

    @Value("${google.api.key}")
    private String googleApiKey;

    @Bean
    public Client AIClient() {
        return Client.builder()
                .apiKey(googleApiKey)
                .build();
    }
}
