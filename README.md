
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

