package com.mobile.prm392.services;

import com.mobile.prm392.config.PayOSConfig;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
public class PayOSService {

    private final WebClient webClient;
    private final PayOSConfig payOSConfig;

    public PayOSService(PayOSConfig payOSConfig) {
        this.payOSConfig = payOSConfig;
        this.webClient = WebClient.builder()
                .baseUrl("https://api-merchant.payos.vn") // endpoint PayOS
                .defaultHeader("x-client-id", payOSConfig.getClientId())
                .defaultHeader("x-api-key", payOSConfig.getApiKey())
                .build();
    }

    public Mono<String> createPaymentLink(Long orderCode, Long amount, String description, String returnUrl, String cancelUrl) {
        Map<String, Object> request = new HashMap<>();
        request.put("orderCode", orderCode);   // phải là Long, không phải String
        request.put("amount", amount);
        request.put("description", description);
        request.put("returnUrl", returnUrl);
        request.put("cancelUrl", cancelUrl);

        return webClient.post()
                .uri("/v2/payment-requests")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class);
    }
}
