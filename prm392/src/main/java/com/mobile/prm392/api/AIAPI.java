package com.mobile.prm392.api;

import com.mobile.prm392.services.AIService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIAPI {

    private final AIService aiService;

    // Model cho request
    @Getter
    public static class ChatRequest {
        private String context;

        public void setContext(String context) {
            this.context = context;
        }
    }

    // Model cho response
    public static class ChatResponse {
        private String reply;
        public ChatResponse(String reply) {
            this.reply = reply;
        }
        public String getReply() {
            return reply;
        }
    }

    @PostMapping("/chat")
    public ChatResponse askAI(@RequestBody ChatRequest request) {
        String reply = aiService.askAI(request.getContext());
        return new ChatResponse(reply);
    }

    @PostMapping("/flashcard")
    public ChatResponse generateFlashcard() {
        String quote = aiService.generateFlashcard();
        return new ChatResponse(quote);
    }

}
