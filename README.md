# DinnerVery Backend

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
- 영업시간: 15:30 ~ 22:00
- 라스트오더: 21:30
- 배송 가능 시간: 16:00 ~ 22:00 (10분 단위)

## 빌드 및 실행

### 사전 요구사항

- JDK 17 이상
- MySQL 8.4 (운영환경)

### 로컬 실행

#### Gradle을 사용한 실행

```bash
# 의존성 설치 및 빌드
./gradlew build

# 애플리케이션 실행
./gradlew bootRun
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


## VIP 할인 정책

- **등급 기준**: 월 15회 이상 주문 시 VIP 등급
- **할인 혜택**: VIP 등급 시 상시 10% 할인 적용
- **초기화**: VIP 시작일로부터 1개월 후 주문수 초기화
