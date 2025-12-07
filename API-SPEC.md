# DinnerVery API 명세서

## 개요
프론트엔드에서 메뉴, 메뉴 옵션, 서빙 스타일 정보를 직접 관리하며, 백엔드는 주문 처리 및 재고 관리만 담당합니다.

## Base URL
```
http://localhost:8080/api
```

## 인증
대부분의 API는 JWT 토큰을 필요로 합니다. 토큰은 `Authorization` 헤더에 `Bearer {token}` 형식으로 전달합니다.

---

## 1. 인증 API

### 1.1 고객 회원가입
**POST** `/api/auth/customer/signup`

**Request Body:**
```json
{
  "loginId": "string",
  "password": "string",
  "name": "string",
  "phoneNumber": "string",
  "address": "string"
}
```

**필드 타입:**
- `loginId`: `String` (필수)
- `password`: `String` (필수)
- `name`: `String` (필수)
- `phoneNumber`: `String` (선택)
- `address`: `String` (필수)

**Response:** `200 OK`
```json
{
  "customerId": 1,
  "loginId": "string",
  "name": "string",
  "phoneNumber": "string",
  "grade": "BASIC",
  "orderCount": 0
}
```

**필드 타입:**
- `customerId`: `Long`
- `loginId`: `String`
- `name`: `String`
- `phoneNumber`: `String`
- `grade`: `String` ("BASIC" | "VIP")
- `orderCount`: `Integer`

### 1.2 통합 로그인
**POST** `/api/auth/login`

**Request Body:**
```json
{
  "loginId": "string",
  "password": "string"
}
```

**필드 타입:**
- `loginId`: `String` (필수)
- `password`: `String` (필수)

**Response:** `200 OK`

**고객 로그인 시:**
```json
{
  "userId": 1,
  "loginId": "string",
  "name": "string",
  "role": "CUSTOMER",
  "token": "string",
  "grade": "BASIC" | "VIP",
  "task": null
}
```

**직원 로그인 시:**
```json
{
  "userId": 1,
  "loginId": "string",
  "name": "string",
  "role": "COOK" | "DELIVERY",
  "token": "string",
  "grade": null,
  "task": "COOK" | "DELIVERY"
}
```

**필드 타입:**
- `userId`: `Long`
- `loginId`: `String`
- `name`: `String`
- `role`: `String` ("CUSTOMER" | "COOK" | "DELIVERY")
- `token`: `String`
- `grade`: `String | null` ("BASIC" | "VIP", 고객이 아닌 경우 `null`)
- `task`: `String | null` ("COOK" | "DELIVERY", 직원이 아닌 경우 `null`)

### 1.3 고객 정보 조회
**GET** `/api/auth/customer/{customerId}`

**Path Parameters:**
- `customerId`: `Long`

**Headers:**
- `Authorization: Bearer {token}`

**Response:** `200 OK`
```json
{
  "customerId": 1,
  "loginId": "string",
  "name": "string",
  "phoneNumber": "string",
  "grade": "BASIC" | "VIP",
  "orderCount": 0
}
```

**필드 타입:**
- `customerId`: `Long`
- `loginId`: `String`
- `name`: `String`
- `phoneNumber`: `String`
- `grade`: `String` ("BASIC" | "VIP")
- `orderCount`: `Integer`

**참고:** 
- 기본 등급은 `BASIC`입니다.
- 주문 횟수가 15회 이상이면 자동으로 `VIP` 등급으로 승급됩니다.

---

## 2. 장바구니 API

### 2.1 장바구니에 상품 추가
**POST** `/api/cart/{customerId}/items`

**Path Parameters:**
- `customerId`: `Long`

**Headers:**
- `Authorization: Bearer {token}`

**Request Body:**
```json
{
  "menuId": 1,
  "menuName": "발렌타인 디너",
  "menuPrice": 28000,
  "menuQuantity": 2,
  "styleId": 1,
  "styleName": "SIMPLE",
  "styleExtraPrice": 0,
  "options": [
    {
      "optionId": 1,
      "optionName": "스테이크",
      "optionPrice": 15000,
      "defaultQty": 1,
      "quantity": 2
    },
    {
      "optionId": 2,
      "optionName": "와인",
      "optionPrice": 8000,
      "defaultQty": 1,
      "quantity": 1
    }
  ]
}
```

**필드 타입:**
- `menuId`: `Long` (필수) - 메뉴 ID (프론트에서 관리)
- `menuName`: `String` (필수) - 메뉴 이름
- `menuPrice`: `Integer` (필수, 0 이상) - 메뉴 기본 가격
- `menuQuantity`: `Integer` (필수, 1 이상) - 메뉴 수량
- `styleId`: `Long` (필수) - 서빙 스타일 ID (프론트에서 관리)
- `styleName`: `String` (필수) - 서빙 스타일 이름 (예: "SIMPLE", "GRAND", "DELUXE")
- `styleExtraPrice`: `Integer` (필수, 0 이상) - 서빙 스타일 추가 가격
- `options`: `Array<OptionRequest>` (선택) - 옵션 배열
  - `optionId`: `Long` (필수) - 옵션 ID (프론트에서 관리)
  - `optionName`: `String` (필수) - 옵션 이름 (재고 매칭에 사용됨, Storage의 name과 일치해야 함)
  - `optionPrice`: `Integer` (필수, 0 이상) - 옵션 단가
  - `defaultQty`: `Integer` (필수, 1 이상) - 기본 수량
  - `quantity`: `Integer` (필수, 1 이상) - 주문 수량
  
**참고:** 재고 소비량(`storageConsumption`)은 백엔드에서 자동으로 처리됩니다. 프론트엔드는 전달할 필요가 없습니다.

**Response:** `200 OK`
```json
{
  "cartItemId": 1,
  "menu": {
    "menuId": 1,
    "name": "발렌타인 디너",
    "quantity": 2,
    "unitPrice": 28000
  },
  "style": {
    "styleId": 1,
    "name": "SIMPLE",
    "price": 0
  },
  "options": [
    {
      "optionId": 1,
      "name": "스테이크",
      "quantity": 2,
      "unitPrice": 15000
    },
    {
      "optionId": 2,
      "name": "와인",
      "quantity": 1,
      "unitPrice": 8000
    }
  ],
  "totalAmount": 106000
}
```

**필드 타입:**
- `cartItemId`: `Long`
- `menu`: `Object`
  - `menuId`: `Long`
  - `name`: `String`
  - `quantity`: `Integer`
  - `unitPrice`: `Integer`
- `style`: `Object`
  - `styleId`: `Long`
  - `name`: `String`
  - `price`: `Integer`
- `options`: `Array<Object>`
  - `optionId`: `Long`
  - `name`: `String`
  - `quantity`: `Integer`
  - `unitPrice`: `Integer`
- `totalAmount`: `Integer`

### 2.2 장바구니 조회
**GET** `/api/cart/{customerId}`

**Path Parameters:**
- `customerId`: `Long`

**Headers:**
- `Authorization: Bearer {token}`

**Response:** `200 OK`
```json
{
    "totalAmount": 116000,
    "cartId": 6,
    "customerId": 1,
    "cartItems": [
        {
            "dinnerItem": {
                "unitPrice": 28000,
                "quantity": 2,
                "name": "발렌타인 디너",
                "menuId": 1
            },
            "options": [
                {
                    "unitPrice": 15000,
                    "quantity": 3,
                    "defaultQty": 1,
                    "extraPrice": 30000,
                    "name": "스테이크",
                    "optionId": 1
                },
                {
                    "unitPrice": 8000,
                    "quantity": 1,
                    "defaultQty": 1,
                    "extraPrice": 0,
                    "name": "와인",
                    "optionId": 2
                }
            ],
            "style": {
                "styleId": 1,
                "extraPrice": 0,
                "name": "SIMPLE"
            },
            "cartItemId": 2
        }
    ]
}
```

**필드 타입:**
- `totalAmount`: `Integer`
- `cartId`: `Long | null`
- `customerId`: `Long`
- `cartItems`: `Array<Object>`
  - `cartItemId`: `Long`
  - `dinnerItem`: `Object`
    - `menuId`: `Long`
    - `name`: `String`
    - `quantity`: `Integer`
    - `unitPrice`: `Integer`
  - `style`: `Object`
    - `styleId`: `Long`
    - `name`: `String`
    - `extraPrice`: `Integer`
  - `options`: `Array<Object>`
    - `optionId`: `Long`
    - `name`: `String`
    - `quantity`: `Integer`
    - `defaultQty`: `Integer`
    - `unitPrice`: `Integer`
    - `extraPrice`: `Integer`

### 2.3 장바구니 옵션 수량 변경
**PATCH** `/api/cart/{customerId}/items/{cartItemId}/options/{optionId}`

**Headers:**
- `Authorization: Bearer {token}`

**Request Body:**
```json
{
  "quantity": 3
}
```

**필드 타입:**
- `quantity`: `Integer` (필수, 1 이상)

**Path Parameters:**
- `customerId`: `Long`
- `cartItemId`: `Long`
- `optionId`: `Long`

**Response:** `200 OK`
```json
{
    "totalAmount": 116000,
    "itemTotal": 116000,
    "cartItemId": 2,
    "option": {
        "unitPrice": 15000,
        "quantity": 3,
        "name": "스테이크",
        "optionId": 1
    }
}
```

**필드 타입:**
- `totalAmount`: `Integer`
- `itemTotal`: `Integer`
- `cartItemId`: `Long`
- `option`: `Object`
  - `optionId`: `Long`
  - `name`: `String`
  - `quantity`: `Integer`
  - `unitPrice`: `Integer`

### 2.4 장바구니 아이템 삭제
**DELETE** `/api/cart/{customerId}/items`

**Path Parameters:**
- `customerId`: `Long`

**Query Parameters:**
- `cartItemId`: `Long` (선택) - 삭제할 장바구니 아이템 ID
  - 제공하지 않거나 `0`인 경우: 전체 장바구니 삭제
  - 특정 값인 경우: 해당 아이템만 삭제

**Headers:**
- `Authorization: Bearer {token}`

**사용 예시:**
- 특정 아이템 삭제: `DELETE /api/cart/1/items?cartItemId=5`
- 전체 장바구니 삭제: `DELETE /api/cart/1/items` 또는 `DELETE /api/cart/1/items?cartItemId=0`

**참고:** 
- `cartItemId`가 제공되지 않거나 `0`이면 해당 고객의 장바구니에 있는 모든 아이템을 삭제합니다 (로그아웃 시 사용).
- 특정 `cartItemId`가 제공되면 해당 아이템과 관련된 모든 옵션도 함께 삭제됩니다.

**Response (특정 아이템 삭제 시):** `200 OK`
```json
{
  "message": "장바구니 아이템이 삭제되었습니다.",
  "remainingItemsCount": 2
}
```

**Response (전체 장바구니 삭제 시):** `200 OK`
```json
{
  "message": "장바구니가 모두 삭제되었습니다.",
  "cartItems": []
}
```

**필드 타입:**
- `message`: `String` - 삭제 완료 메시지
- `remainingItemsCount`: `Integer` (특정 아이템 삭제 시) - 삭제 후 남은 아이템 개수
- `cartItems`: `Array` (전체 삭제 시) - 항상 빈 배열

---

## 3. 주문 API

### 3.1 주문 생성
**POST** `/api/orders`

**Headers:**
- `Authorization: Bearer {token}`

**Request Body:**
```json
{
  "customerId": 1,
  "address": "서울시 강남구 테헤란로 123",
  "cardNumber": "1234-5678-9012-3456",
  "deliveryTime": "18:00:00"
}
```

**필드 타입:**
- `customerId`: `Long` (필수)
- `address`: `String` (필수)
- `cardNumber`: `String` (필수)
- `deliveryTime`: `String` (필수, 형식: "HH:mm:ss" 또는 "HH:mm")

**Response:** `201 Created`
```json
{
    "orderId": 3,
    "customerId": 1,
    "status": "REQUESTED",
    "totalPrice": 116000,
    "orderItems": [
        {
            "menuId": 1,
            "name": "발렌타인 디너",
            "quantity": 2,
            "price": 58000,
            "subTotal": 116000,
            "style": {
                "styleId": 1,
                "name": "SIMPLE",
                "price": 0
            },
            "options": [
                {
                    "optionId": 1,
                    "name": "스테이크",
                    "quantity": 3,
                    "defaultQty": 1,
                    "price": 15000,
                    "extraPrice": 30000
                },
                {
                    "optionId": 2,
                    "name": "와인",
                    "quantity": 1,
                    "defaultQty": 1,
                    "price": 8000,
                    "extraPrice": 0
                }
            ]
        }
    ],
    "createdAt": "2025-12-07T02:14:55.1716783",
    "deliveryTime": "19:40"
}
```

**필드 타입:**
- `orderId`: `Long`
- `customerId`: `Long`
- `status`: `String` ("REQUESTED" | "COOKING" | "COOKED" | "DELIVERING" | "DONE")
- `totalPrice`: `Integer`
- `orderItems`: `Array<Object>`
  - `menuId`: `Long`
  - `name`: `String`
  - `quantity`: `Integer`
  - `price`: `Integer`
  - `subTotal`: `Integer`
  - `style`: `Object`
    - `styleId`: `Long`
    - `name`: `String`
    - `price`: `Integer`
  - `options`: `Array<Object>`
    - `optionId`: `Long`
    - `name`: `String`
    - `quantity`: `Integer`
    - `defaultQty`: `Integer`
    - `price`: `Integer`
    - `extraPrice`: `Integer`
- `createdAt`: `String` (ISO 8601 형식: "yyyy-MM-ddTHH:mm:ss")
- `deliveryTime`: `String` (형식: "HH:mm")

**참고:** 주문 생성 시 장바구니의 모든 상품이 주문으로 변환되며, 장바구니는 비워집니다.

### 3.2 요리 중인 주문 조회 (요리사용)
**GET** `/api/orders/cooking`

**Headers:**
- `Authorization: Bearer {token}` (COOK 권한 필요)

**Response:** `200 OK`
```json
{
    "orders": [
        {
            "orderId": 3,
            "status": "REQUESTED",
            "deliveryTime": "19:40",
            "orderItems": [
                {
                    "menuId": 1,
                    "name": "발렌타인 디너",
                    "quantity": 2,
                    "styleId": 1,
                    "styleName": "SIMPLE",
                    "options": [
                        {
                            "optionId": 1,
                            "name": "스테이크",
                            "quantity": 3
                        },
                        {
                            "optionId": 2,
                            "name": "와인",
                            "quantity": 1
                        }
                    ]
                }
            ]
        }
    ]
}
```

**필드 타입:**
- `orders`: `Array<Object>`
  - `orderId`: `Long`
  - `status`: `String` ("REQUESTED" | "COOKING")
  - `deliveryTime`: `String` (형식: "HH:mm")
  - `orderItems`: `Array<Object>`
    - `menuId`: `Long`
    - `name`: `String`
    - `quantity`: `Integer`
    - `styleId`: `Long`
    - `styleName`: `String`
    - `options`: `Array<Object>`
      - `optionId`: `Long`
      - `name`: `String`
      - `quantity`: `Integer`

**참고:** 
- 이 API는 `REQUESTED` 또는 `COOKING` 상태의 주문만 반환합니다.
- 각 주문의 `status`는 `"REQUESTED"` 또는 `"COOKING"` 중 하나입니다.

### 3.3 배송 중인 주문 조회 (배달원용)
**GET** `/api/orders/delivery`

**Headers:**
- `Authorization: Bearer {token}` (DELIVERY 권한 필요)

**Response:** `200 OK`
```json
{
    "orders": [
        {
            "orderId": 3,
            "status": "COOKED",
            "deliveryTime": "19:40",
            "address": "짱구 배달 주소",
            "orderItems": [
                {
                    "menuId": 1,
                    "name": "발렌타인 디너",
                    "quantity": 2,
                    "styleId": 1,
                    "styleName": "SIMPLE",
                    "options": [
                        {
                            "optionId": 1,
                            "name": "스테이크",
                            "quantity": 3
                        },
                        {
                            "optionId": 2,
                            "name": "와인",
                            "quantity": 1
                        }
                    ]
                }
            ]
        }
    ]
}
```

**필드 타입:**
- `orders`: `Array<Object>`
  - `orderId`: `Long`
  - `status`: `String` ("COOKED" | "DELIVERING")
  - `deliveryTime`: `String` (형식: "HH:mm")
  - `address`: `String`
  - `orderItems`: `Array<Object>`
    - `menuId`: `Long`
    - `name`: `String`
    - `quantity`: `Integer`
    - `styleId`: `Long`
    - `styleName`: `String`
    - `options`: `Array<Object>`
      - `optionId`: `Long`
      - `name`: `String`
      - `quantity`: `Integer`

**참고:** 
- 이 API는 `COOKED` 또는 `DELIVERING` 상태의 주문만 반환합니다.
- 각 주문의 `status`는 `"COOKED"` 또는 `"DELIVERING"` 중 하나입니다.

### 3.4 고객 주문 내역 조회
**GET** `/api/orders/customer/{customerId}`

**Headers:**
- `Authorization: Bearer {token}`

**Path Parameters:**
- `customerId`: `Long`

**Response:** `200 OK`
```json
{
    "orders": [
        {
            "deliveryTime": "19:40",
            "orderId": 3,
            "totalPrice": 116000,
            "orderDate": "2025.12.07",
            "orderItems": [
                {
                    "quantity": 2,
                    "name": "발렌타인 디너",
                    "options": [
                        {
                            "quantity": 3,
                            "name": "스테이크"
                        },
                        {
                            "quantity": 1,
                            "name": "와인"
                        }
                    ],
                    "styleName": "SIMPLE"
                }
            ],
            "status": "COOKED"
        }
    ]
}
```

**필드 타입:**
- `orders`: `Array<Object>`
  - `orderId`: `Long`
  - `totalPrice`: `Integer`
  - `orderDate`: `String` (형식: "yyyy.MM.dd")
  - `status`: `String` ("REQUESTED" | "COOKING" | "COOKED" | "DELIVERING" | "DONE")
  - `deliveryTime`: `String` (형식: "HH:mm")
  - `orderItems`: `Array<Object>`
    - `name`: `String`
    - `quantity`: `Integer`
    - `styleName`: `String`
    - `options`: `Array<Object>`
      - `name`: `String`
      - `quantity`: `Integer`

### 3.5 주문 상태 변경
**PATCH** `/api/orders/{id}/status`

**Headers:**
- `Authorization: Bearer {token}` (COOK 또는 DELIVERY 권한 필요)

**Path Parameters:**
- `id`: `Long`

**Request Body:**
```json
{
  "status": "COOKING" | "COOKED" | "DELIVERING" | "DONE"
}
```

**필드 타입:**
- `status`: `String` (필수, "COOKING" | "COOKED" | "DELIVERING" | "DONE")

**상태 변경 흐름:**
- `REQUESTED` → `COOKING` → `COOKED` → `DELIVERING` → `DONE`
- `COOKED` 상태로 변경 시 재고가 자동으로 차감됩니다.
  - 주문 옵션의 `optionName`과 Storage의 `name`을 매칭
  - 백엔드에서 재고 소비량을 계산하여 차감 (기본적으로 `quantity`만큼 차감)

**Response:** `200 OK`
```json
{
  "orderId": 1,
  "status": "COOKED"
}
```

**필드 타입:**
- `orderId`: `Long`
- `status`: `String` ("REQUESTED" | "COOKING" | "COOKED" | "DELIVERING" | "DONE")

---

## 4. 재고 API

### 4.1 재고 조회
**GET** `/api/storage`

**Headers:**
- `Authorization: Bearer {token}`

**Response:** `200 OK`
```json
{
    "storageItems": [
        {
            "quantity": 97,
            "name": "고기",
            "storageId": 1
        },
        {
            "quantity": 99,
            "name": "와인",
            "storageId": 2
        },
        {
            "quantity": 100,
            "name": "채소",
            "storageId": 3
        },
        {
            "quantity": 100,
            "name": "커피",
            "storageId": 4
        },
        {
            "quantity": 100,
            "name": "샴페인",
            "storageId": 5
        },
        {
            "quantity": 100,
            "name": "바게트빵",
            "storageId": 6
        },
        {
            "quantity": 100,
            "name": "계란",
            "storageId": 7
        }
    ]
}
```

**필드 타입:**
- `storageItems`: `Array<Object>`
  - `storageId`: `Long`
  - `name`: `String`
  - `quantity`: `Integer`

**참고:** 재고는 매일 오전 5시에 자동으로 100으로 리셋됩니다.

---

## 5. AI 음성인식 API

### 5.1 음성인식 주문 처리
**POST** `/api/ai/order`

**Headers:**
- `Authorization: Bearer {token}`

**Request Body:**
```json
{
  "text": "발렌타인 디너 2개 주문해줘"
}
```

**필드 타입:**
- `text`: `String` (필수) - 음성 인식된 텍스트 또는 사용자 입력

**Response:** `200 OK`
```json
{
  "success": true,
  "result": {
    "reply": "발렌타인 디너 2개를 주문 목록에 추가했습니다.",
    "state": "ordering",
    "orderSummary": {
      "menuId": 1,
      "menuName": "발렌타인 디너",
      "quantity": 2
    }
  },
  "error": null
}
```

**에러 발생 시:** `400 Bad Request`
```json
{
  "success": false,
  "result": null,
  "error": "AI 서비스 호출 실패: ..."
}
```

**필드 타입:**
- `success`: `Boolean` - 요청 성공 여부
- `result`: `Object | null` - 성공 시 응답 데이터
  - `reply`: `String` - AI 응답 메시지
  - `state`: `String` - 현재 상태 (예: "ordering", "confirming" 등)
  - `orderSummary`: `Object | null` - 주문 요약 정보
    - `menuId`: `Long` - 메뉴 ID
    - `menuName`: `String` - 메뉴 이름
    - `quantity`: `Integer` - 수량
- `error`: `String | null` - 에러 발생 시 에러 메시지

**참고:**
- 이 API는 내부적으로 AI 서비스(`http://ai-service:8000/chat`)를 호출합니다.
- AI 서비스가 주문 정보를 파싱하여 `orderSummary`를 반환합니다.
- `orderSummary`가 `null`이 아닌 경우, 프론트엔드에서 해당 정보를 사용하여 장바구니에 추가할 수 있습니다.

---

## 6. 재고 관리 로직

### 재고 차감 규칙
주문 상태가 `COOKED`로 변경될 때 자동으로 재고가 차감됩니다:

1. 주문의 모든 `OrderItemOption`을 순회
2. 각 옵션의 `optionName`과 Storage의 `name`을 **정확히 일치**시켜 매칭
3. 매칭된 Storage의 재고에서 `quantity` 만큼 차감 (재고 소비량은 백엔드에서 관리)
4. 재고가 부족하면 예외 발생: `"재고가 부족하여 요리를 완료할 수 없습니다: {재고명} 재고가 부족합니다."`
5. 매칭되는 Storage가 없으면 재고 차감을 건너뜁니다 (에러 없음)

**중요:** 옵션 이름과 Storage 이름은 **정확히 일치**해야 합니다.
- 옵션 이름이 "스테이크"이고 Storage 이름이 "고기"이면 매칭되지 않습니다.
- 옵션 이름이 "와인"이고 Storage 이름이 "와인"이면 매칭됩니다.

**프론트엔드에서 주의사항:**
- 옵션 이름을 설정할 때 Storage 이름과 일치시켜야 합니다.
- 예: Storage에 "고기"가 있으면, 옵션 이름도 "고기"로 설정해야 재고가 차감됩니다.
- 또는 옵션 이름을 Storage 이름과 매핑하는 로직을 프론트에서 관리해야 합니다.

---

## 7. 제거된 API

다음 API들은 더 이상 제공되지 않습니다 (프론트에서 직접 관리):

- `GET /api/menus` - 메뉴 목록 조회
- `GET /api/menus/{menuId}/options` - 메뉴 옵션 조회
- `GET /api/styles` - 서빙 스타일 목록 조회

프론트엔드에서 메뉴, 메뉴 옵션, 서빙 스타일 정보를 직접 관리하고, 주문 시 해당 정보를 함께 전달해야 합니다.

---

## 8. 에러 응답

### 공통 에러 형식
```json
{
  "timestamp": "2025-12-06T14:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "에러 메시지",
  "path": "/api/endpoint"
}
```

**필드 타입:**
- `timestamp`: `String` (ISO 8601 형식: "yyyy-MM-ddTHH:mm:ss")
- `status`: `Integer` (HTTP 상태 코드)
- `error`: `String` (에러 타입)
- `message`: `String` (에러 메시지)
- `path`: `String` (요청 경로)

### 주요 에러 코드
- `400 Bad Request`: 잘못된 요청 (필수 필드 누락, 유효하지 않은 값 등)
- `401 Unauthorized`: 인증 실패
- `403 Forbidden`: 권한 없음
- `404 Not Found`: 리소스를 찾을 수 없음
- `500 Internal Server Error`: 서버 내부 오류

---

## 9. 중요 사항

### 프론트엔드에서 관리해야 하는 정보
1. **메뉴 정보**
   - 메뉴 ID, 이름, 가격

2. **서빙 스타일 정보**
   - 스타일 ID, 이름, 추가 가격

3. **메뉴 옵션 정보**
   - 옵션 ID, 이름, 가격, 기본 수량
   - **참고**: 재고 소비량은 백엔드에서 자동으로 처리되므로 프론트엔드에서 관리할 필요가 없습니다.

### 재고 매칭 규칙
- 주문 옵션의 `optionName`과 Storage의 `name`을 정확히 일치시켜야 합니다.
- 예: 옵션 이름이 "와인"이면 Storage 이름도 "와인"이어야 합니다.
- 프론트엔드에서 옵션 이름을 설정할 때 Storage 이름과 일치시켜야 합니다.
- 재고 소비량은 백엔드에서 자동으로 계산하여 처리됩니다.

### 주문 생성 시 주의사항
- 장바구니에 상품이 있어야 주문을 생성할 수 있습니다.
- 주문 생성 후 장바구니는 자동으로 비워집니다.
- 주문 상태가 `COOKED`로 변경될 때 재고가 차감됩니다.

