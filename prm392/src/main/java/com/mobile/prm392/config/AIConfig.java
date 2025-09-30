package com.mobile.prm392.config;

import com.google.genai.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIConfig {
    @Bean
    public Client AIClient() {
        return Client.builder()
                .apiKey(System.getenv("GOOGLE_API_KEY"))
                .build();
    }
}
