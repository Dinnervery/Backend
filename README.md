# DinnerVery Backend

## 기술 스택
- Java 17, Gradle
- Spring Boot 3.5.x (Web, Data JPA, Validation)
- Spring Security (JWT 기반 인증/인가)
- Spring Data JPA (Hibernate)
- MySQL (프로덕션) / H2 (개발)

## 로컬 실행

#### Windows (CMD/PowerShell)
```
.\gradlew.bat clean bootRun
```

#### Linux/Mac
```
./gradlew clean bootRun
```

#### H2 Console
```
URL: http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:dinnervery
Username: sa
```

## 주요 API

### 인증
- **통합 로그인**: `POST /api/auth/login` (고객/직원 자동 구분)
- **고객 회원가입**: `POST /api/auth/customer/signup`
- **고객 정보 조회**: `GET /api/auth/customer/{customerId}`

### 메뉴
- **메뉴 목록**: `GET /api/menus`
- **메뉴 옵션**: `GET /api/menus/{menuId}/options`
- **스타일 목록**: `GET /api/styles`

### 장바구니
- **장바구니 조회**: `GET /api/cart/{customerId}`
- **장바구니 추가**: `POST /api/cart/{customerId}/items`

### 주문
- **주문 생성**: `POST /api/orders`
- **주문 목록**: `GET /api/orders/customer/{customerId}`

자세한 API 문서는 `API-ENDPOINTS.md`를 참고하세요.
