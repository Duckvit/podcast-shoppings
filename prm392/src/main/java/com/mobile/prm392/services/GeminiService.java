package com.mobile.prm392.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobile.prm392.model.gemini.GeminiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class GeminiService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GeminiService(@Value("${spring.ai.gemini.api-key}") String apiKey) {
        this.webClient = WebClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-pro:generateContent")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultUriVariables(Map.of("key", apiKey))
                .build();
    }

    public GeminiResponse askGemini(String prompt) {
        String body = """
            {
              "contents": [{ "parts":[{ "text": "%s" }]}]
            }
            """.formatted(prompt);

        String rawResponse = webClient.post()
                .uri("?key={key}")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            String text = root.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

            return new GeminiResponse(text);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi parse JSON từ Gemini API", e);
        }
    }
}
