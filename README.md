# DinnerVery Backend

![Java CI (Gradle)](https://github.com/ssub17/test/actions/workflows/ci.yml/badge.svg)

## 프로젝트 소개

DinnerVery는 디너 주문 및 배달 서비스를 제공하는 백엔드 애플리케이션입니다.

## 기술 스택

- **Java 17**
- **Spring Boot 3.x**
- **Spring Data JPA**
- **H2 Database** (테스트)
- **Gradle 8.14.3**

## 빌드 및 실행

### 사전 요구사항

- JDK 17 이상
- Gradle 8.14.3 이상

### 로컬 실행

```bash
# 의존성 설치
./gradlew build

# 애플리케이션 실행
./gradlew bootRun
```

### 테스트 실행

```bash
# 모든 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests "CustomerGradeUpdateTest"
```

## 환경 설정

### 프로필별 설정

- `application.yml`: 기본 설정
- `application-prod.yml`: 운영 환경 설정
- `application-test.yml`: 테스트 환경 설정

### 환경 변수

- `SPRING_PROFILES_ACTIVE`: 활성 프로필 설정 (dev, prod, test)

## API 엔드포인트

### 기본 URL
- 로컬 실행: `http://localhost:8080`
- API 문서: `http://localhost:8080/swagger-ui.html` (Swagger 설정 시)

### 주요 API
- `/api/members`: 회원 관리
- `/api/menus`: 메뉴 관리
- `/api/orders`: 주문 관리
- `/api/carts`: 장바구니 관리

## 데이터베이스

### 테스트 환경
- H2 인메모리 데이터베이스 사용
- 테스트 실행 시 자동으로 스키마 생성/삭제

### 운영 환경
- MySQL 또는 PostgreSQL 권장

## VIP 할인 정책

- 15회 이상 주문 시 VIP 등급
- VIP 등급 시 상시 10% 할인 적용
- 월별 초기화 (VIP 시작일로부터 1개월 후)

## 개발 가이드

### 코드 스타일
- Google Java Style Guide 준수
- Lombok 사용

### 테스트
- JUnit 5 사용
- Spring Boot Test 활용
- Mockito를 통한 단위 테스트

## 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다.
