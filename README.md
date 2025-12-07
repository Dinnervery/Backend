# DinnerVery Backend

미스터 대박 디너 주문 및 배달 관리를 위한 백엔드 서버입니다. 고객은 장바구니에 디너를 담고 주문할 수 있으며, 직원(요리사/배달원)은 주문 상태를 관리하고 재고를 확인할 수 있습니다.

## 기술 스택

- Java 17, Gradle
- Spring Boot 3.5.6 (Web, Data JPA, Validation, Security)
- Spring Data JPA (Hibernate)
- MySQL 8.x
- JWT 인증

## 실행 방법

1. 환경 변수를 설정합니다.

   ```
   DB_HOST=<HOST>
   DB_PORT=3306
   DB_NAME=<DATABASE_NAME>
   DB_USERNAME=<USER>
   DB_PASSWORD=<PASSWORD>
   JWT_SECRET=<SECRET_KEY>
   ```

2. 의존성을 내려받고 애플리케이션을 실행합니다.

   ```bash
   ./gradlew bootRun
   ```

3. 기본 포트 `8080`에서 API를 호출할 수 있습니다.

## API 개요

모든 API는 `/api` 하위 경로에 존재하며, 대부분의 API는 JWT 토큰 인증이 필요합니다.

### 인증

#### POST `/api/auth/login`
고객/직원 통합 로그인. 응답의 `role` 필드로 구분됩니다 (`CUSTOMER`, `COOK`, `DELIVERY`).

#### POST `/api/auth/customer/signup`
고객 회원가입.

### 장바구니

#### GET `/api/cart/{customerId}`
장바구니 조회. 여러 디너 아이템과 총액을 반환합니다.

#### POST `/api/cart/{customerId}/items`
장바구니에 디너 추가.

#### DELETE `/api/cart/{customerId}/items?cartItemId={id}`
- `cartItemId`가 있으면: 해당 아이템만 삭제
- `cartItemId`가 없거나 `0`이면: 전체 장바구니 삭제

### 주문

#### POST `/api/orders`
장바구니의 모든 아이템을 주문으로 생성. 주문 생성 시 장바구니는 자동으로 비워집니다.

#### GET `/api/orders/customer/{customerId}`
고객의 주문 내역 조회. 모든 상태(`REQUESTED`, `COOKING`, `COOKED`, `DELIVERING`, `DONE`)의 주문을 반환합니다.

#### GET `/api/orders/cooking`
요리사용. `REQUESTED`, `COOKING` 상태의 주문만 반환합니다.

#### GET `/api/orders/delivery`
배달원용. `COOKED`, `DELIVERING` 상태의 주문만 반환합니다.

#### PATCH `/api/orders/{id}/status`
주문 상태 변경. `COOKING`, `COOKED`, `DELIVERING`, `DONE` 중 하나로 변경 가능합니다. `COOKED` 상태로 변경 시 재고가 자동으로 차감됩니다.

### 재고

#### GET `/api/storage`
재고 목록 조회.

자세한 API 명세는 `API-SPEC.md`를 참고하세요.
