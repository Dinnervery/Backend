# 백엔드에서 orderSummary 파싱 과정 예시

## 1. 입력 예시

**AI 서비스 응답:**
```json
{
  "reply": "샴페인 디너로 변경하겠습니다. 샴페인 디너는 항상 2인 세트로 샴페인 1잔, 스테이크 2개가 포함됩니다.",
  "state": "RUNNING",
  "orderSummary": null
}
```

## 2. 파싱 과정

### Step 1: 메뉴 이름 추출
```java
Pattern menuPattern = Pattern.compile("(샴페인 디너|발렌타인 디너)");
Matcher matcher = menuPattern.matcher(reply);
// 결과: "샴페인 디너"
```

**정규식 설명:**
- `(샴페인 디너|발렌타인 디너)`: 두 메뉴 이름 중 하나를 찾음
- `|`: OR 연산자

### Step 2: 수량 추출
```java
Pattern quantityPattern = Pattern.compile("(\\d+)(?:인 세트|개|명)");
Matcher matcher = quantityPattern.matcher(reply);
// 결과: "2인 세트" → 2
```

**정규식 설명:**
- `(\\d+)`: 숫자 캡처 (1개 이상)
- `(?:인 세트|개|명)`: 비캡처 그룹 (매칭만 하고 캡처 안 함)
- 예: "2인 세트", "3개", "1명" 등

**대체 로직:**
```java
if (reply.contains("2인 세트") || reply.contains("항상 2인")) {
    quantity = 2;
} else {
    quantity = 1; // 기본값
}
```

### Step 3: 스타일 이름 추출
```java
Pattern stylePattern = Pattern.compile("(SIMPLE|GRAND|DELUXE)\\s*스타일");
Matcher matcher = stylePattern.matcher(reply);
// 결과: "GRAND 스타일" → "GRAND"
```

**정규식 설명:**
- `(SIMPLE|GRAND|DELUXE)`: 세 가지 스타일 중 하나
- `\\s*`: 0개 이상의 공백
- `스타일`: 리터럴 문자열

### Step 4: 옵션 정보 추출
```java
// 알려진 옵션 목록
String[] knownOptions = {
    "스테이크", "와인", "샴페인", "에그 스크램블", 
    "바게트빵", "베이컨", "샐러드", "커피", "커피포트"
};

for (String optionName : knownOptions) {
    Pattern optionPattern = Pattern.compile(
        optionName + "\\s*(\\d+)(?:잔|개|인|명)?"
    );
    Matcher matcher = optionPattern.matcher(reply);
    // "샴페인 1잔" → optionName: "샴페인", quantity: 1
    // "스테이크 2개" → optionName: "스테이크", quantity: 2
}
```

**정규식 설명:**
- `optionName`: 옵션 이름 (예: "샴페인", "스테이크")
- `\\s*`: 0개 이상의 공백
- `(\\d+)`: 수량 (숫자)
- `(?:잔|개|인|명)?`: 단위 (선택적)

## 3. 최종 결과

```java
OrderSummary orderSummary = new OrderSummary(
    null,                    // menuId: 알 수 없음
    "샴페인 디너",           // menuName: 파싱 결과
    2,                       // quantity: "2인 세트"에서 추출
    null,                    // styleId: 알 수 없음
    null,                    // styleName: 이 예시에는 없음
    Arrays.asList(
        new OptionInfo(null, "샴페인", 1),    // "샴페인 1잔"
        new OptionInfo(null, "스테이크", 2)  // "스테이크 2개"
    )
);
```

## 4. 실제 파싱 코드 구조

```java
private OrderSummary extractOrderSummaryFromReply(String reply) {
    // 1. 메뉴 이름 추출
    String menuName = extractMenuName(reply);
    if (menuName == null) {
        return null; // 메뉴 이름이 없으면 생성 불가
    }
    
    // 2. 수량 추출
    Integer quantity = extractQuantity(reply);
    
    // 3. 스타일 이름 추출
    String styleName = extractStyleName(reply);
    
    // 4. 옵션 정보 추출
    List<OrderSummary.OptionInfo> options = extractOptions(reply);
    
    // 5. OrderSummary 생성
    return new OrderSummary(
        null, menuName, quantity,
        null, styleName,
        options.isEmpty() ? null : options
    );
}
```

## 5. 한계점

1. **ID 정보 부족**: `menuId`, `optionId`, `styleId`는 알 수 없음
   - 프론트엔드에서 이름으로 매핑 필요

2. **하드코딩된 패턴**: 알려진 메뉴/옵션/스타일 이름만 추출 가능
   - 새로운 항목 추가 시 패턴 업데이트 필요

3. **텍스트 파싱의 불확실성**: 
   - AI 응답 형식이 바뀌면 파싱 실패 가능
   - 다양한 표현 방식 처리 어려움 (예: "2개", "두 개", "2인분")

4. **컨텍스트 부족**:
   - 이전 대화 내용 모름
   - 페이지별 상태 모름

## 6. 더 나은 방법

**파이썬 AI 서비스에서 직접 orderSummary 생성:**
- AI가 구조화된 JSON으로 응답
- 메뉴/옵션/스타일 정보를 정확히 파싱
- ID 정보도 함께 반환 가능 (프론트엔드 정보와 매칭)

---

## 7. 백엔드에 디너/옵션/스타일 정보가 있다면?

만약 백엔드에 `Menu`, `Option`, `Style` 엔티티와 Repository가 있다면, 파싱한 이름으로 ID를 매핑하여 완전한 `orderSummary`를 생성할 수 있습니다.

### 7.1 가정: 엔티티 구조

```java
@Entity
public class Menu {
    @Id
    private Long id;
    private String name;  // "샴페인 디너", "발렌타인 디너"
    private int price;
}

@Entity
public class Option {
    @Id
    private Long id;
    private String name;  // "스테이크", "와인", "샴페인"
    private int price;
    private int defaultQty;
}

@Entity
public class Style {
    @Id
    private Long id;
    private String name;  // "SIMPLE", "GRAND", "DELUXE"
    private int extraPrice;
}
```

### 7.2 개선된 파싱 코드

```java
@Service
@RequiredArgsConstructor
public class OrderSummaryService {
    
    private final MenuRepository menuRepository;
    private final OptionRepository optionRepository;
    private final StyleRepository styleRepository;
    
    public OrderSummary extractOrderSummaryFromReply(String reply) {
        // 1. 메뉴 이름 추출 및 ID 매핑
        String menuName = extractMenuName(reply);
        Menu menu = menuRepository.findByName(menuName)
            .orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없습니다: " + menuName));
        
        // 2. 수량 추출
        Integer quantity = extractQuantity(reply);
        
        // 3. 스타일 이름 추출 및 ID 매핑
        String styleName = extractStyleName(reply);
        Style style = null;
        if (styleName != null) {
            style = styleRepository.findByName(styleName)
                .orElse(null);  // 스타일이 없어도 계속 진행
        }
        
        // 4. 옵션 정보 추출 및 ID 매핑
        List<OrderSummary.OptionInfo> options = extractOptionsWithId(reply);
        
        // 5. 완전한 OrderSummary 생성
        return new OrderSummary(
            menu.getId(),           // ✅ menuId: DB에서 조회
            menu.getName(),         // menuName
            quantity,
            style != null ? style.getId() : null,  // ✅ styleId: DB에서 조회
            style != null ? style.getName() : null,  // styleName
            options.isEmpty() ? null : options
        );
    }
    
    private List<OrderSummary.OptionInfo> extractOptionsWithId(String reply) {
        List<OrderSummary.OptionInfo> options = new ArrayList<>();
        
        // 알려진 옵션 이름 목록
        String[] knownOptions = {
            "스테이크", "와인", "샴페인", "에그 스크램블", 
            "바게트빵", "베이컨", "샐러드", "커피", "커피포트"
        };
        
        for (String optionName : knownOptions) {
            Pattern optionPattern = Pattern.compile(
                optionName + "\\s*(\\d+)(?:잔|개|인|명)?"
            );
            Matcher matcher = optionPattern.matcher(reply);
            
            if (matcher.find()) {
                int qty = Integer.parseInt(matcher.group(1));
                
                // ✅ 옵션 이름으로 DB에서 조회하여 ID 매핑
                Option option = optionRepository.findByName(optionName)
                    .orElse(null);
                
                if (option != null) {
                    options.add(new OrderSummary.OptionInfo(
                        option.getId(),  // ✅ optionId: DB에서 조회
                        option.getName(), // optionName
                        qty
                    ));
                }
            }
        }
        
        return options;
    }
}
```

### 7.3 최종 결과 (개선됨)

```java
OrderSummary orderSummary = new OrderSummary(
    2L,                      // ✅ menuId: DB에서 조회 (예: "샴페인 디너" → 2)
    "샴페인 디너",           // menuName
    2,                       // quantity
    3L,                      // ✅ styleId: DB에서 조회 (예: "GRAND" → 3)
    "GRAND",                 // styleName
    Arrays.asList(
        new OptionInfo(5L, "샴페인", 1),      // ✅ optionId: DB에서 조회
        new OptionInfo(1L, "스테이크", 2)    // ✅ optionId: DB에서 조회
    )
);
```

### 7.4 장점

1. **완전한 정보**: 모든 ID가 포함된 완전한 `orderSummary`
2. **프론트엔드 매핑 불필요**: 백엔드에서 모든 정보 제공
3. **데이터 일관성**: DB에 저장된 정보와 일치
4. **유효성 검증**: 존재하지 않는 메뉴/옵션/스타일은 에러 처리 가능

### 7.5 주의사항

1. **이름 매칭의 정확성**: 
   - DB의 이름과 파싱한 이름이 정확히 일치해야 함
   - 대소문자, 공백 등에 주의

2. **Repository 메서드 필요**:
   ```java
   public interface MenuRepository extends JpaRepository<Menu, Long> {
       Optional<Menu> findByName(String name);
   }
   
   public interface OptionRepository extends JpaRepository<Option, Long> {
       Optional<Option> findByName(String name);
   }
   
   public interface StyleRepository extends JpaRepository<Style, Long> {
       Optional<Style> findByName(String name);
   }
   ```

3. **에러 처리**:
   - 메뉴를 찾을 수 없으면 예외 발생 또는 null 반환
   - 옵션이나 스타일은 선택적이므로 null 허용 가능
