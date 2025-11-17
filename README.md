# DinnerVery Backend

## 기술 스택
- Java 17, Gradle
- Spring Boot 3.5.x (Web, Data JPA, Validation)
- Spring Security (JWT 기반 인증/인가)
- Spring Data JPA (Hibernate)
- H2

## 로컬 실행

#### Windows (CMD/PowerShell)
.\gradlew.bat clean bootRun

#### Linux/Mac
./gradlew clean bootRun

#### H2 Console
URL: http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:dinnervery
Username: sa
