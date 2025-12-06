# AI 서비스 API 명세서

## 개요
AI 서비스는 음성 인식을 통한 주문 처리를 담당합니다. 프론트엔드는 백엔드 API를 통해 AI 서비스를 호출하며, 백엔드는 내부적으로 AI 서비스와 통신합니다.

## 아키텍처
```
프론트엔드 → 백엔드 (/api/ai/*) → AI 서비스 (http://ai-service:8000/*)
또는
프론트엔드 → Nginx (/ai/*) → AI 서비스 (http://127.0.0.1:8000/*)
```

---

## 1. 백엔드 AI API (프론트엔드 호출용)

### 1.1 AI 주문 처리
음성 인식을 통한 주문 처리를 요청합니다.

**엔드포인트**
```
POST /api/ai/order
```

**인증**
- ❌ 인증 불필요 (SecurityConfig에서 `/api/ai/**`는 `permitAll()`)

**요청 헤더**
```
Content-Type: application/json
```

**요청 본문**
```json
{
  "text": "김치찌개 2개 주문할게요"
}
```

**요청 필드**
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| text | String | ✅ | 사용자 입력 텍스트 (음성 인식 결과 또는 직접 입력) |

**응답 (성공)**
```json
{
  "success": true,
  "result": {
    "reply": "김치찌개 2개를 주문 목록에 추가했습니다.",
    "state": "ordering",
    "orderSummary": null
  },
  "error": null
}
```

**응답 (주문 완료 시)**
```json
{
  "success": true,
  "result": {
    "reply": "주문이 완료되었습니다.",
    "state": "completed",
    "orderSummary": "김치찌개 x2"
  },
  "error": null
}
```

**응답 (실패)**
```json
{
  "success": false,
  "result": null,
  "error": "AI 서비스 호출 실패: Connection refused"
}
```

**응답 필드**
| 필드 | 타입 | 설명 |
|------|------|------|
| success | Boolean | 요청 성공 여부 |
| result | Object \| null | AI 서비스 응답 (JSON 객체, 실패 시 null) |
| error | String \| null | 에러 메시지 (실패 시) |

**result 객체 구조**
| 필드 | 타입 | 설명 |
|------|------|------|
| reply | String | AI 응답 메시지 |
| state | String | 대화 상태 (`ordering`, `confirming`, `completed`) |
| orderSummary | String \| null | 주문 요약 (주문 완료 시에만 제공) |

**상태 코드**
- `200 OK`: 성공
- `400 Bad Request`: 요청 형식 오류 또는 AI 서비스 호출 실패

**예시 (cURL)**
```bash
curl -X POST http://223.130.132.136/api/ai/order \
  -H "Content-Type: application/json" \
  -d '{"text": "김치찌개 2개 주문할게요"}'
```

---

### 1.2 AI 서비스 헬스 체크
AI 서비스의 상태를 확인합니다.

**엔드포인트**
```
GET /api/ai/health
```

**인증**
- ❌ 인증 불필요

**요청 헤더**
```
없음
```

**요청 본문**
```
없음
```

**응답 (성공)**
```
{"status": "healthy"}
```

**응답 (실패)**
```
AI 서비스 연결 실패: Connection refused
```

**상태 코드**
- `200 OK`: AI 서비스 정상
- `503 Service Unavailable`: AI 서비스 연결 실패

**예시 (cURL)**
```bash
curl http://223.130.132.136/api/ai/health
```

---

## 2. AI 서비스 직접 API (백엔드 내부 호출)

### 2.1 채팅/주문 처리
AI 대화 관리자를 통한 주문 처리

**엔드포인트**
```
POST /chat
```

**기본 URL**
- Docker 내부: `http://ai-service:8000`
- 로컬 테스트: `http://localhost:8000`

**요청 헤더**
```
Content-Type: application/json
```

**요청 본문**
```json
{
  "text": "김치찌개 2개 주문할게요"
}
```

**요청 필드**
| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| text | String | ✅ | 사용자 입력 텍스트 |

**응답 (성공)**
```json
{
  "reply": "김치찌개 2개를 주문 목록에 추가했습니다.",
  "state": "ordering",
  "order_summary": null
}
```

**응답 (주문 완료 시)**
```json
{
  "reply": "주문이 완료되었습니다.",
  "state": "completed",
  "order_summary": "김치찌개 x2"
}
```

**응답 필드**
| 필드 | 타입 | 설명 |
|------|------|------|
| reply | String | AI 응답 메시지 |
| state | String | 대화 상태 (`ordering`, `confirming`, `completed`) |
| order_summary | String \| null | 주문 요약 (주문 완료 시에만 제공) |

**상태 코드**
- `200 OK`: 성공
- `400 Bad Request`: 요청 형식 오류
- `500 Internal Server Error`: AI 서비스 내부 오류

---

### 2.2 헬스 체크
AI 서비스의 상태를 확인합니다.

**엔드포인트**
```
GET /health
```

**기본 URL**
- Docker 내부: `http://ai-service:8000`
- 로컬 테스트: `http://localhost:8000`

**요청 헤더**
```
없음
```

**요청 본문**
```
없음
```

**응답 (성공)**
```json
{
  "status": "healthy"
}
```

**상태 코드**
- `200 OK`: 정상

**예시 (Docker 내부)**
```bash
docker exec dinnervery-ai python -c "import urllib.request; print(urllib.request.urlopen('http://localhost:8000/health').read().decode())"
```

---

## 3. Nginx를 통한 직접 접근 (선택사항)

Nginx 설정에 따라 프론트엔드에서 AI 서비스를 직접 호출할 수 있습니다.

### 3.1 AI 서비스 직접 호출
```
POST http://223.130.132.136/ai/chat
GET http://223.130.132.136/ai/health
```

**주의사항**
- Nginx 설정에서 `/ai/` 경로가 `http://127.0.0.1:8000/`로 프록시되어 있어야 합니다.
- 일반적으로는 백엔드를 통한 호출(`/api/ai/*`)을 권장합니다.

---

## 4. 환경 변수

### 백엔드 설정
```yaml
# application.yml
ai:
  base-url: ${AI_BASE_URL:http://ai-service:8000}
```

### Docker Compose 설정
```yaml
services:
  backend:
    environment:
      - AI_BASE_URL=http://ai-service:8000
  
  ai-service:
    environment:
      - PORT=8000
      - GROQ_API_KEY=${GROQ_API_KEY}
```

---

## 5. 에러 처리

### 백엔드 → AI 서비스 호출 실패
- **원인**: AI 서비스가 다운되었거나 네트워크 문제
- **응답**: `400 Bad Request` with `{"success": false, "error": "..."}`
- **대응**: AI 서비스 로그 확인 및 재시작

### AI 서비스 내부 오류
- **원인**: AI 모델 호출 실패, 의존성 오류 등
- **응답**: `500 Internal Server Error`
- **대응**: AI 서비스 로그 확인

---

## 6. 보안 설정

### 인증
- `/api/ai/**` 경로는 `permitAll()`로 설정되어 인증 불필요
- 필요 시 인증 추가 가능

### CORS
- 백엔드: `http://localhost:3000` 허용
- AI 서비스: 모든 Origin 허용 (`allow_origins=["*"]`)

---

## 7. 테스트 방법

### 1. AI 서비스 직접 테스트
```bash
# Docker 내부에서
docker exec dinnervery-ai python -c "
import urllib.request
import json
req = urllib.request.Request('http://localhost:8000/chat', 
    data=json.dumps({'text': '김치찌개 2개'}).encode(),
    headers={'Content-Type': 'application/json'})
print(urllib.request.urlopen(req).read().decode())
"
```

### 2. 백엔드를 통한 테스트
```bash
# 프론트엔드 또는 Postman에서
POST http://223.130.132.136/api/ai/order
Content-Type: application/json

{
  "text": "김치찌개 2개 주문할게요"
}
```

### 3. 헬스 체크 테스트
```bash
# 백엔드 API
curl http://223.130.132.136/api/ai/health

# AI 서비스 직접
curl http://localhost:8000/health  # 서버 내부에서만 가능
```

---

## 8. 주의사항

1. **네트워크**: AI 서비스는 Docker 네트워크 내부에서만 접근 가능 (`ai-service:8000`)
2. **포트**: AI 서비스는 외부 포트를 공개하지 않음 (내부 통신만)
3. **응답 형식**: AI 서비스 응답은 JSON 객체로 직접 반환되므로 파싱 불필요
4. **비동기 처리**: 백엔드 API는 `Mono`를 사용하므로 비동기 처리됨

---

## 9. 변경 이력

- 2024-12-06: 초기 API 명세 작성
- AI 서비스 통합 완료
- Health check 엔드포인트 추가
- 2024-12-06: DTO 클래스 추가로 응답 형식 개선 (JSON 객체로 직접 반환)

