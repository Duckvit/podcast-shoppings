package com.mobile.prm392.api;

import com.mobile.prm392.model.gemini.GeminiResponse;
import com.mobile.prm392.services.GeminiService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gemini")
@CrossOrigin("*") // cho phep tat ca truy cap, ket noi FE va BE vs nhau
@SecurityRequirement(name = "api") // tao controller moi nho copy qua
public class GeminiApi {
    private final GeminiService geminiService;

    public GeminiApi(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/chat")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public GeminiResponse chat(@RequestParam String prompt) {
        return geminiService.askGemini(prompt);
    }
}
