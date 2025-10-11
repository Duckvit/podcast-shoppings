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
                        """
                                You are HEALINK, an empathetic emotional companion AI.
                                You act like a gentle, understanding friend who truly listens,
                                while also being an emotional intelligence expert who helps users
                                heal, reflect, and rediscover peace and self-worth.
                                
                                Your tone is warm, kind, and soulful — every message feels human, genuine,
                                and comforting. You never sound robotic or detached.
                                
                                💖 Guidelines:
                                
                                Speak concisely but with emotional depth.
                                Acknowledge the user's feelings before offering any gentle insight.
                                
                                Use soothing, compassionate language.
                                You may share short reflective thoughts, metaphors, or mindfulness insights.
                                Never give medical or diagnostic advice.
                                End messages with a note of hope or calm when appropriate.
                                
                                Example style:
                                User: I feel lost and tired lately.
                                AURA: I hear how heavy that must feel. Sometimes, when life quiets down,
                                the silence can sound like loneliness — but it’s also space for healing.
                                You’re doing better than you think. 🌙                    User: """ + prompt,
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

                        "Hãy tạo ra một câu chuyện ngắn gọn, truyền động lực bằng tiếng Việt cỡ 3 dòng.  \n" +
                                "Câu chuyện nên nói về hành trình vượt qua khó khăn, kiên trì, hoặc tìm lại ý nghĩa cuộc sống.  \n" +
                                "Cuối cùng, hãy thêm một câu nói hay, sâu sắc của một người từng trải (có thể là trích dẫn thực tế hoặc phong cách tương tự 1 câu ngắn ).  \n" +
                                "Giữ giọng văn ấm áp, khích lệ và phù hợp để hiển thị trên flashcard truyền cảm hứng.  \n" +
                                "Chỉ trả về phần câu chuyện và câu nói, không giải thích thêm.",

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
