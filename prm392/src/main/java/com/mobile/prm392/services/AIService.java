package com.mobile.prm392.services;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AIService {
    private final Client client;

    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 2000; // 2s

    // Chat healing
    public String askAI(String prompt) {
        GenerateContentResponse response =
                client.models.generateContent(
                        "gemini-2.5-flash",
                        "You are a gentle Healing Companion AI; always reply with short, empathetic, and comforting messages that bring peace, hope, and encouragement, without giving medical advice.\nUser: "
                                + prompt,
                        null);
        return response.text();
    }

    // Flashcard motivational quote
    public String generateFlashcard() {
        GenerateContentResponse response =
                client.models.generateContent(
                        "gemini-2.5-flash",
//                        "Generate one short motivational or healing quote for a flashcard. " +
//                                "Keep it concise, uplifting, and suitable to display on a card. " +
//                                "Do not add explanations, only return the quote text.",

                        "Hãy tạo một câu nói ngắn gọn, truyền cảm hứng hoặc chữa lành bằng tiếng Việt. " +
                                "Câu phải ngắn, dễ nhớ, phù hợp để hiển thị trên flashcard. " +
                                "Chỉ trả về câu nói, không giải thích gì thêm.",

                        null);
        return response.text();
    }

    // Common retry logic
    private String callGeminiWithRetry(String prompt) {
        int attempt = 0;
        while (attempt < MAX_RETRIES) {
            try {
                GenerateContentResponse response = client.models.generateContent(
                        "gemini-2.5-flash",
                        prompt,
                        null);
                return response.text();
            } catch (Exception e) {
                attempt++;
                if (attempt >= MAX_RETRIES) {
                    throw new RuntimeException("Gemini API đang quá tải hoặc không phản hồi. Vui lòng thử lại sau.", e);
                }
                try {
                    Thread.sleep(RETRY_DELAY_MS); // chờ 2s trước khi retry
                } catch (InterruptedException ignored) {
                }
            }
        }
        throw new RuntimeException("Gemini API failed after retries.");
    }
}
