package com.dinnervery.controller;

import com.dinnervery.dto.ai.response.AiOrderResponse;
import com.dinnervery.service.AiServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiServiceClient aiServiceClient;

    /**
     * AI 서비스를 통한 주문 처리 (음성 인식)
     * 프론트엔드에서 POST /api/ai/order 호출
     * 백엔드 내부에서 http://ai-service:8000/chat 호출
     */
    @PostMapping("/order")
    public Mono<ResponseEntity<AiOrderResponse>> processOrder(@RequestBody Map<String, String> request) {
        String input = request.get("text"); // 또는 request.get("audio") 등
        
        return aiServiceClient.callAi(input)
                .map(response -> {
                    // AI 서비스 응답을 DTO로 반환
                    AiOrderResponse result = new AiOrderResponse(true, response, null);
                    return ResponseEntity.ok(result);
                })
                .onErrorResume(error -> {
                    // 에러 발생 시
                    AiOrderResponse errorResult = new AiOrderResponse(false, null, error.getMessage());
                    return Mono.just(ResponseEntity.badRequest().body(errorResult));
                });
    }

    /**
     * AI 서비스 Health Check
     */
    @GetMapping("/health")
    public Mono<ResponseEntity<String>> healthCheck() {
        return aiServiceClient.healthCheck()
                .map(ResponseEntity::ok)
                .onErrorResume(error -> {
                    return Mono.just(ResponseEntity.status(503)
                            .body("AI 서비스 연결 실패: " + error.getMessage()));
                });
    }
}

