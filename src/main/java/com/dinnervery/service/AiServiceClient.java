package com.dinnervery.service;

import com.dinnervery.dto.ai.response.AiChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiServiceClient {

    private final WebClient aiWebClient;

    /**
     * AI 서비스 호출 (음성 인식 주문 처리)
     * @param request 요청 데이터 (예: 음성 텍스트 또는 주문 정보)
     * @return AI 서비스 응답 (DTO 객체)
     */
    public Mono<AiChatResponse> callAi(String request) {
        // AI 서비스의 /chat 엔드포인트 호출
        // 요청 형식: {"text": "..."}
        Map<String, String> requestBody = Map.of("text", request);
        return aiWebClient.post()
                .uri("/chat")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(AiChatResponse.class)
                .onErrorResume(error -> {
                    // 에러 처리
                    return Mono.error(new RuntimeException("AI 서비스 호출 실패: " + error.getMessage()));
                });
    }

    /**
     * AI 서비스 Health Check
     */
    public Mono<String> healthCheck() {
        return aiWebClient.get()
                .uri("/health")
                .retrieve()
                .bodyToMono(String.class);
    }
}

