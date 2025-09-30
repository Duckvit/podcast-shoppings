package com.mobile.prm392.config;

import com.google.genai.Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIConfig {
//    @Bean
//    public Client AIClient() {
//        return Client.builder()
//                .apiKey(System.getenv("GOOGLE_API_KEY"))
//                .build();
//    }
@Value("${spring.ai.gemini.api-key}")
private String apiKey;

    @Bean
    public Client AIClient() {
        return Client.builder()
                .apiKey(apiKey)  // lấy từ application.properties
                .build();
    }
}
