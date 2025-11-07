# DinnerVery Backend

### 로컬 실행

#### Gradle을 사용한 실행

# (권장) 의존성 설치, 빌드, 실행을 한번에
# Windows (CMD/PowerShell)
.\gradlew.bat clean bootRun

# Linux/Mac
./gradlew clean bootRun

---

# (참고) 빌드와 실행을 따로 할 경우

# 1. 의존성 설치 및 빌드
# Windows (CMD/PowerShell)
.\gradlew.bat build

# Linux/Mac
./gradlew build

# 2. 애플리케이션 실행
# Windows (CMD/PowerShell)
.\gradlew.bat bootRun

# Linux/Mac
./gradlew bootRun


### H2 Console
http://localhost:8080/h2-console/ 접속
JDBC URL : jdbc:h2:mem:dinnervery
