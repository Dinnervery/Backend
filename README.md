# DinnerVery Backend

![Java CI/CD](https://github.com/ssub17/test/actions/workflows/ci.yml/badge.svg)
![Code Coverage](https://codecov.io/gh/ssub17/test/branch/main/graph/badge.svg)

## 프로젝트 소개

DinnerVery는 디너 주문 및 배달 서비스를 제공하는 백엔드 애플리케이션입니다. 고객의 주문부터 배달까지의 전체 프로세스를 관리하며, VIP 등급 시스템과 할인 정책을 포함합니다.

## 기술 스택

- **Java 17**
- **Spring Boot 3.5.6**
- **Spring Data JPA**
- **MySQL 8.4** (운영환경)
- **H2 Database** (개발/테스트)
- **Gradle 8.5**
- **Docker & Docker Compose**
- **GitHub Actions** (CI)

## 주요 기능

### 🍽️ 주문 관리
- 주문 생성, 조회, 완료
- 주문 상태 관리 (REQUESTED → COOKING → COOKED → DELIVERING → DONE)
- 가격 계산 및 VIP 할인 적용
- 재주문 기능

### 👥 고객 관리
- 고객 등록
- VIP 등급 시스템 (월 15회 이상 주문 시 VIP 등급)
- VIP 고객 10% 할인 혜택

### 🍴 메뉴 관리
- 메뉴 목록 조회
- 서빙 스타일 선택 (추가비용 포함)
- 메뉴 옵션 관리

### ⏰ 영업시간 관리
- 영업시간: 오후 3:30 ~ 오후 10:00
- 라스트오더: 오후 9:30
- 배송 가능 시간: 오후 4:00 ~ 오후 10:00 (10분 단위)

## 빌드 및 실행

### 사전 요구사항

- JDK 17 이상
- Docker & Docker Compose (선택사항)
- MySQL 8.4 (운영환경)

### 로컬 실행

#### 1. Gradle을 사용한 실행

```bash
# 의존성 설치 및 빌드
./gradlew build

# 애플리케이션 실행
./gradlew bootRun
```

#### 2. Docker를 사용한 실행

```bash
# Docker Compose로 전체 스택 실행
docker-compose up -d

# 로그 확인
docker-compose logs -f app
```

### 테스트 실행

```bash
# 모든 테스트 실행
./gradlew test

# 코드 커버리지 포함 테스트
./gradlew test jacocoTestReport

# 특정 테스트 클래스 실행
./gradlew test --tests "CustomerGradeUpdateTest"
```

## 환경 설정

### 프로필별 설정

- `application.yml`: 기본 설정 (H2 인메모리 DB)
- `application-prod.yml`: 운영 환경 설정 (MySQL)
- `application-test.yml`: 테스트 환경 설정

### 환경 변수

```bash
# 데이터베이스 설정
DB_NAME=dinnervery
DB_USERNAME=dinner
DB_PASSWORD=dinnerpwd
DB_ROOT_PASSWORD=rootpwd

# 애플리케이션 설정
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080
```

## API 엔드포인트

### 기본 URL
- 로컬 실행: `http://localhost:8080`
- API 문서: `http://localhost:8080/swagger-ui.html` (Swagger 설정 시)

### 주요 API

#### 주문 관리
- `POST /api/orders` - 주문 생성
- `GET /api/orders/{id}` - 주문 조회
- `PATCH /api/orders/{id}/status` - 주문 상태 변경
- `POST /api/orders/{id}/reorder` - 재주문

#### 고객 관리
- `POST /api/customers` - 고객 등록

#### 메뉴 관리
- `GET /api/menus` - 메뉴 목록 조회
- `GET /api/serving-styles` - 서빙 스타일 조회

#### 장바구니 관리
- `POST /api/carts/{customerId}/items` - 장바구니에 아이템 추가
- `GET /api/carts/{customerId}` - 장바구니 조회

## 데이터베이스

### 개발 환경
- H2 인메모리 데이터베이스 사용
- 테스트 실행 시 자동으로 스키마 생성/삭제
- H2 콘솔: `http://localhost:8080/h2-console`

### 운영 환경
- MySQL 8.4 사용
- Docker Compose로 MySQL 컨테이너 실행 가능

## VIP 할인 정책

- **등급 기준**: 월 15회 이상 주문 시 VIP 등급
- **할인 혜택**: VIP 등급 시 상시 10% 할인 적용
- **초기화**: VIP 시작일로부터 1개월 후 주문수 초기화

## CI 파이프라인

### GitHub Actions 워크플로우

**CI Pipeline** (`.github/workflows/ci.yml`)
- ✅ **JaCoCo**: 코드 커버리지 측정 (70% 목표)
- ✅ **빌드 및 테스트**: MySQL 통합 테스트

## 개발 가이드

### 코드 스타일
- Google Java Style Guide 준수
- Lombok 사용으로 보일러플레이트 코드 제거

### 테스트 전략
- **단위 테스트**: JUnit 5 + Mockito
- **통합 테스트**: Spring Boot Test
- **API 테스트**: 실제 HTTP 요청 테스트
- **커버리지 목표**: 70% 이상

### Git 워크플로우
- `main`: 프로덕션 브랜치
- `develop`: 개발 브랜치
- Feature 브랜치에서 개발 후 PR 생성

## 모니터링 및 로깅

### Health Check
- Spring Boot Actuator 사용
- `/actuator/health` 엔드포인트 제공

### 로깅
- SLF4J + Logback 사용
- 환경별 로그 레벨 설정
- 구조화된 로그 출력

## 배포

### Docker 배포
```bash
# 프로덕션 이미지 빌드
docker build -t dinnervery-backend:latest .

# Docker Compose로 배포
docker-compose -f docker-compose.yml up -d
```

## 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다.
