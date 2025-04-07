# hhplus e-commerce API Docs

![](https://img.shields.io/static/v1?label=&message=GET&color=blue)
![](https://img.shields.io/static/v1?label=&message=POST&color=brightgreen)
![](https://img.shields.io/static/v1?label=&message=PUT&color=orange)
![](https://img.shields.io/static/v1?label=&message=PATCH&color=pink)
![](https://img.shields.io/static/v1?label=&message=DELETE&color=red)

## Point

### 포인트 충전 API

> ![](https://img.shields.io/static/v1?label=&message=POST&color=brightgreen) <br>
> `/api/v1/points/charge`

<details markdown="1">

<summary>스펙 상세</summary>

#### Paramters

**Body**

|      필드명       | 데이터 타입 |        설명         |  필수여부  | 유효성 검사                |
|:--------------:|:------:|:-----------------:|:------:|:----------------------|
|    `userId`    | Number | 포인트를 충전하는 사용자 식별자 | **필수** | 양의 정수                 | 
| `chargeAmount` | Number |  충전하고자 하는 포인트 금액  | **필수** | 0보다 크면서 1,000,000원 이하 |

**Example Reuqest Body**

```json
{
  "userId": 1,
  "chargeAmount": 100000
}
```

#### Response

<details markdown="1">
<summary>200 OK : 성공적으로 충전된 경우</summary>

|      필드명       | 데이터 타입 |     설명     |
|:--------------:|:------:|:----------:|
|     `code`     | Number | HTTP 상태 코드 |
|   `message`    | String | 요청 처리 메시지  |
|     `data`     | Object |   응답 데이터   |
| `data.userId`  | Number | 충전된 사용자 ID |
| `data.balance` | Number |  충전 후 잔액   |

```json
{
  "code": 200,
  "message": "요칭이 정상적으로 처리되었습니다.",
  "data": {
    "userId": 1,
    "balance": 1000000
  }
}
```

</details>

<details markdown="1">
<summary>409 Conflict : 1회 충전 금액을 초과해서 충전이 실패한 경우</summary>

```json
{
  "code": 409,
  "message": "비즈니스 정책을 위반한 요청입니다.",
  "detail": "1회 충전 금액은 1,000,000원을 초과할 수 없습니다. 입력값 : 1,500,000원"
}
```

</details>

<details markdown="1">
<summary>409 Conflict : 누적 충전 금액을 초과해서 충전이 실패한 경우</summary>
</details>

```json
{
  "code": 409,
  "message": "비즈니스 정책을 위반한 요청입니다.",
  "detail": "누적 충전 금액은 5,000,000원을 초과할 수 없습니다. 현재 누적 충전 금액 : 5,000,000원"
}
```

</details>
<br>

### 포인트 조회 API

> ![](https://img.shields.io/static/v1?label=&message=GET&color=blue) <br>
> `/api/v1/points?userId={userId}`

<details markdown="1">

<summary>스펙 상세</summary>

#### Paramters

**Query Params**

|   필드명    | 데이터 타입 |        설명        |  필수여부  | 유효성 검사 |
|:--------:|:------:|:----------------:|:------:|:-------|
| `userId` | Number | 포인트를 조회하는 사용자 ID | **필수** | 양의 정수  |

#### Response

<details markdown="1">
<summary>200 OK: 성공적으로 조회된 경우</summary>

|      필드명       | 데이터 타입 |     설명     |
|:--------------:|:------:|:----------:|
|     `code`     | Number | HTTP 상태 코드 |
|   `message`    | String | 요청 처리 메시지  |
|     `data`     | Object |   응답 데이터   |
| `data.userId`  | Number | 조회된 사용자 ID |
| `data.balance` | Number |   조회된 잔액   |

```json
{
  "code": 200,
  "message": "요청이 정상적으로 처리되었습니다.",
  "data": {
    "userId": 1,
    "balance": 1000000
  }
}

```

</details>
</details>

### 포인트 사용 API (결제)

> ![](https://img.shields.io/static/v1?label=&message=POST&color=brightgreen) <br>
> `/api/v1/points/use`

<details markdown="1">

<summary>스펙 상세</summary>

#### Body

|    필드명    | 데이터 타입 |       설명       |  필수여부  | 유효성 검사 |
|:---------:|:------:|:--------------:|:------:|:-------|
| `orderId` | Number | 사용자가 주문한 주문 ID | **필수** | 양의 정수  |

**Example Request Body**

```json
{
  "orderId": 1
}
```

#### Response

<details markdown="1">
<summary>204 No Content : 결제가 성공적으로 완료된 경우</summary>

</details>

<details markdown="1">
<summary>409 Conflict : 결제 금액이 포인트보다 커서 결제가 실패한 경우</summary>

```json
{
  "code": 409,
  "message": "비즈니스 정책을 위반한 요청입니다.",
  "detail": "포인트 잔액이 부족합니다. 현재 잔액 : 100,000원, 결제 금액 : 200,000원"
}
```

</details>

<details markdown="1">
<summary>409 Conflict : 주문 상태가 EXPIRED(결제 유효 기간 만료)이기 때문에 결제가 실패한 경우</summary>

```json
{
  "code": 409,
  "message": "비즈니스 정책을 위반한 요청입니다.",
  "detail": "주문 상태가 EXPIRED(결제 불가 건)입니다."
}
```

</details>
</details>
<br>

## Product

### 상품 목록 조회 API

> ![](https://img.shields.io/static/v1?label=&message=GET&color=blue) <br>
> `/api/v1/products`

<details markdown="1"> 
<summary>스펙 상세</summary>

#### Response

<details markdown="1">
<summary>200 OK : 성공적으로 조회된 경우</summary>

|          필드명          | 데이터 타입 |     설명     |
|:---------------------:|:------:|:----------:|
|        `code`         | Number | HTTP 상태 코드 |
|       `message`       | String | 요청 처리 메시지  |
|        `data`         | Object |   응답 데이터   |
|    `data.products`    | Array  |   상품 목록    |
|  `product.productId`  | Number |   상품 ID    |
| `product.productName` | String |   상품 이름    |
|    `product.price`    | Number |   상품 가격    |
|   `product.stock `    | Number |   상품 재고    |

```json
{
  "code": 200,
  "message": "요청이 정상적으로 처리되었습니다.",
  "data": {
    "products": [
      {
        "productId": 1,
        "productName": "Macbook Pro",
        "price": 2000000,
        "stock": 10
      },
      {
        "productId": 2,
        "productName": "iPhone 12",
        "price": 1200000,
        "stock": 20
      }
    ]
  }
}
```

</details>
</details>
<br>

### 최근 3일간 가장 많이 팔린 인기 상품 5개 조회 API

> ![](https://img.shields.io/static/v1?label=&message=GET&color=blue) <br>
> `/api/v1/products/best`

<details markdown="1">
<summary>스펙 상세</summary>

#### Response

<details markdown="1">
<summary>200 OK : 성공적으로 조회된 경우</summary>

|          필드명          | 데이터 타입 |     설명     |
|:---------------------:|:------:|:----------:|
|        `code`         | Number | HTTP 상태 코드 |
|       `message`       | String | 요청 처리 메시지  |
|        `data`         | Object |   응답 데이터   |
|  `data.bestSellers`   | Array  |   상품 목록    |
|  `product.productId`  | Number |   상품 ID    |
| `product.productName` | String |   상품 이름    |
|    `product.price`    | Number |   상품 가격    |
|    `product.sales`    | Number |   상품 판매량   |
|    `product.stock`    | Number |   상품 재고    |

```json
{
  "code": 200,
  "message": "요청이 정상적으로 처리되었습니다.",
  "data": {
    "bestSellers": [
      {
        "productId": 1,
        "productName": "ice americano",
        "price": 1000,
        "sales": 100,
        "stock": 100
      },
      {
        "productId": 2,
        "productName": "iPhone 12",
        "price": 1200000,
        "sales": 90,
        "stock": 100
      }
    ]
  }
}
```

</details>
</details>
<br>

## Order

### 주문 API

> ![](https://img.shields.io/static/v1?label=&message=POST&color=brightgreen) <br>
> `/api/v1/orders`

<details markdown="1">
<summary>스펙 상세</summary>

### Parameter

#### Body

|            필드명            | 데이터 타입 |               설명                |  필수여부  | 유효성 검사                    |
|:-------------------------:|:------:|:-------------------------------:|:------:|:--------------------------|
|         `userId`          | Number |       주문을 생성한 사용자의 고유 ID        | **필수** | 양의 정수                     | 
|      `userCouponId`       | Number | 사용자가 적용한 쿠폰 ID (없으면 null 또는 생략) | **선택** | 양의 정수                     |
|      `orderProducts`      | Array  |      주문 항목 (상품 ID와 수량의 배열)      | **필수** | 최소 1개 이상의 항목이 있어야 함       |
| `orderProducts.productId` | Number | 사용자가 적용한 쿠폰 ID (없으면 null 또는 생략) | **필수** | 양의 정수                     |
| `orderProducts.quantity`  | Number | 사용자가 적용한 쿠폰 ID (없으면 null 또는 생략) | **필수** | 양의 정수 (최소 1개 이상의 수량이어야 함) |

**Example Reuqest Body**

```json
{
  "userId": 1,
  "userCouponId": 1,
  "orderProducts": [
    {
      "productId": 1,
      "quantity": 2
    },
    {
      "productId": 2,
      "quantity": 1
    }
  ]
}
```

#### Response

<details markdown="1">
<summary>201 Created : 주문이 성공한 경우</summary>

|     필드명      | 데이터 타입 |     설명     |
|:------------:|:------:|:----------:|
|     code     | Number | HTTP 상태 코드 |
|   message    | String | 요청 처리 메시지  |
|     data     | Object |   응답 데이터   |
| data.orderId | Number |   주문 ID    |

```json
{
  "code": 201,
  "status": "Created",
  "message": "리소스가 성공적으로 생성되었습니다.",
  "data": {
    "orderId": 1
  }
}
```

</details>

<details markdown="1">
<summary>409 Conflict : 쿠폰을 적용하였으나 보유한 쿠폰이 아니면 주문이 실패한 경우</summary>

```json
{
  "code": 409,
  "message": "비즈니스 정책을 위반한 요청입니다.",
  "detail": "사용자가 보유한 쿠폰이 아닙니다."
}
```

</details>

<details markdown="1">
<summary>409 Conflict : 쿠폰이 유효한 기간이 아니라서 주문이 실패한 경우</summary>

```json
{
  "code": 409,
  "message": "비즈니스 정책을 위반한 요청입니다.",
  "detail": "쿠폰이 유효한 기간이 아닙니다."
}
```

</details>

<details markdown="1">
<summary>409 Conflict : 이미 사용된 쿠폰을 적용하려고 해서 주문이 실패한 경우</summary>

```json
{
  "code": 409,
  "message": "비즈니스 정책을 위반한 요청입니다.",
  "detail": "이미 사용된 쿠폰입니다."
}
```

</details>

<details markdown="1">
<summary>409 Conflict : 재고가 부족해서 주문이 실패한 경우</summary>

```json
{
  "code": 409,
  "message": "비즈니스 정책을 위반한 요청입니다.",
  "detail": "상품의 재고가 부족합니다."
}
```

</details>
</details>
<br>

## Coupon

### 사용자 보유 쿠폰 조회 API

> ![](https://img.shields.io/static/v1?label=&message=GET&color=blue) <br>
> `/api/v1/coupons?userId={userId}`

<details markdown="1">
<summary>스펙 상세</summary>

#### Paramters

**Query Params**

|   필드명    | 데이터 타입 |       설명        |  필수여부  | 유효성 검사 |
|:--------:|:------:|:---------------:|:------:|:-------|
| `userId` | Number | 쿠폰을 조회하는 사용자 ID | **필수** | 양의 정수  |

<details markdown="1">
<summary>200 OK : 성공적으로 조회된 경우</summary>

|          필드명          | 데이터 타입 |               설명                |
|:---------------------:|:------:|:-------------------------------:|
|        `code`         | Number |           HTTP 상태 코드            |
|       `message`       | String |            요청 처리 메시지            |
|        `data`         | Object |             응답 데이터              |
|     `data.userId`     | Number |           조회된 사용자 ID            |
|    `data.coupons`     | Array  |              쿠폰 목록              |
|   `coupon.couponId`   | Number |              쿠폰 ID              |
| `coupon.coupontTitle` | String |              쿠폰 이름              |
| `coupon.discountType` | String | 쿠폰 할인 타입 (RATE: 정률, AMOUNT: 정액) |
|  `coupon.startDate`   | String |             쿠폰 시작일              |
|   `coupon.endDate`    | String |             쿠폰 종료일              |

```json
{
  "code": 200,
  "message": "요청이 정상적으로 처리되었습니다.",
  "data": {
    "userId": 1,
    "coupons": [
      {
        "couponId": 1,
        "coupontTitle": "10% 할인 쿠폰",
        "discountType": "RATE",
        "discountValue": 10,
        "startDate": "2025-08-01",
        "endDate": "2025-08-31"
      },
      {
        "couponId": 2,
        "coupontTitle": "10,000원 할인 쿠폰",
        "discountType": "AMOUNT",
        "discountValue": 10000,
        "startDate": "2025-08-01",
        "endDate": "2025-08-31"
      }
    ]
  }
}
```

</details>
</details>
<br>

### 선착순 쿠폰 발급 API

> ![](https://img.shields.io/static/v1?label=&message=POST&color=brightgreen) <br>
> `/api/v1/coupons/issue`

<details markdown="1">
<summary>스펙 상세</summary>

#### Paramters

**Body**

|    필드명     | 데이터 타입 |       설명        |  필수여부  | 유효성 검사 |
|:----------:|:------:|:---------------:|:------:|:-------|
|  `userId`  | Number | 쿠폰을 발급받는 사용자 ID | **필수** | 양의 정수  |
| `couponId` | Number |   발급받을 쿠폰 ID    | **필수** | 양의 정수  |

**Example Request Body**

```json
{
  "userId": 1,
  "couponId": 1
}
```

<details markdown="1">
<summary>204 No Content : 쿠폰이 성공적으로 발급된 경우</summary>
```
</details>

<details markdown="1">
<summary>409 Conflict : 쿠폰의 잔여 수량이 남지 않아 쿠폰 발급이 실패한 경우</summary>

```json
{
  "code": 409,
  "message": "비즈니스 정책을 위반한 요청입니다.",
  "detail": "쿠폰의 잔여 수량이 부족합니다."
}
```

</details>

<details markdown="1">
<summary>409 Conflict : 이미 쿠폰을 발급 받아 쿠폰 발급이 실패한 경우</summary>

```json
{
  "code": 409,
  "message": "비즈니스 정책을 위반한 요청입니다.",
  "detail": "이미 쿠폰을 발급 받았습니다."
}
```

</details>
</details>




API 에러 상황 정리

### 1️⃣-1 포인트 충전 API

`POST /api/v1/points/charge`

| 에러 상황 | 에러 코드 | 에러 메시지 |
| --- | --- | --- |
| 사용자 ID 유효하지 않음 | 400 | 유효하지 않은 사용자 ID입니다. |
| 충전 금액이 0 이하 | 400 | 충전 금액은 0 보다 커야 합니다. |
| 1회 충전 금액 초과 | 409 | 1회 최대 충전 금액을 초과했습니다. |
| 누적 금액 초과 | 409 | 누적 충전 가능 금액을 초과했습니다. |
| 사용자 존재하지 않음 | 404 | 사용자를 찾을 수 없습니다. |
| 내부 오류 | 500 | 서버 내부 오류가 발생했습니다. |

---

### 1️⃣-2 포인트 잔액 조회 API

`GET /api/v1/points?userId={userId}`

| 에러 상황 | 에러 코드 | 에러 메시지 |
| --- | --- | --- |
| 사용자 ID 유효하지 않음 | 400 | 유효하지 않은 사용자 ID입니다. |
| 내부 오류 | 500 | 서버 내부 오류가 발생했습니다. |

---

### 2️⃣ 기본 상품 목록 조회 API

`GET /api/v1/products`

| 에러 상황 | 에러 코드 | 에러 메시지 |
| --- | --- | --- |
| 내부 오류 | 500 | 서버 내부 오류가 발생했습니다. |

---

### 3️⃣-1 보유 쿠폰 목록 조회 API

`GET /api/v1/coupons?userId={userId}`

| 에러 상황 | 에러 코드 | 에러 메시지 |
| --- | --- | --- |
| 사용자 ID 유효하지 않음 | 400 | 유효하지 않은 사용자 ID입니다. |
| 내부 오류 | 500 | 서버 내부 오류가 발생했습니다. |

---

### 3️⃣-2 선착순 쿠폰 발급 API

`POST /api/v1/coupons/{couponId}/issue`

| 에러 상황 | 에러 코드 | 에러 메시지 |
| --- | --- | --- |
| 쿠폰 ID 유효하지 않음 | 400 | 유효하지 않은 쿠폰 ID입니다. |
| 쿠폰 존재하지 않음 | 404 | 쿠폰을 찾을 수 없습니다. |
| 잔여 수량 0 | 409 | 쿠폰이 모두 소진되었습니다. |
| 이미 발급받음 | 409 | 이미 발급받은 쿠폰입니다. |
| 내부 오류 | 500 | 서버 내부 오류가 발생했습니다. |

---

### 4️⃣-1 주문 생성 API

`POST /api/v1/orders`

| 에러 상황 | 에러 코드 | 에러 메시지 |
| --- | --- | --- |
| 사용자 ID 유효하지 않음 | 400 | 유효하지 않은 사용자 ID입니다. |
| 사용자 존재하지 않음 | 404 | 사용자를 찾을 수 없습니다. |
| 상품 ID가 유효하지 않음 | 400 | 유효하지 않은 상품 ID입니다. |
| 상품 존재하지 않음 | 404 | 상품을 찾을 수 없습니다. |
| 수량 요청 유효하지 않음 | 400 | 수량은 1 이상이어야 합니다. |
| 재고 부족 | 409 | 재고가 부족합니다. |
| 쿠폰 ID 유효하지 않음 | 400 | 유효하지 않은 쿠폰 ID입니다. |
| 쿠폰 보유하지 않음 | 409 | 해당 쿠폰을 보유하고 있지 않습니다. |
| 쿠폰 유효기간 초과 | 409 | 쿠폰 유효기간이 지났거나 아직 유효하지 않습니다. |
| 쿠폰 이미 사용됨 | 409 | 이미 사용된 쿠폰입니다. |
| 내부 오류 | 500 | 서버 내부 오류가 발생했습니다. |

---

### 4️⃣-2 결제 처리 API

`POST /api/v1/points/use`

| 에러 상황 | 에러 코드 | 에러 메시지 |
| --- | --- | --- |
| 사용자 ID 유효하지 않음 | 400 | 유효하지 않은 사용자 ID입니다. |
| 사용자 존재하지 않음 | 404 | 사용자를 찾을 수 없습니다. |
| 주문 ID 유효하지 않음 | 400 | 유효하지 않은 주문 ID입니다. |
| 주문 존재하지 않음 | 404 | 주문을 찾을 수 없습니다. |
| 주문 상태가 결제 불가능 | 409 | 결제가 불가능한 주문 상태입니다. |
| 포인트 부족 | 409 | 포인트가 부족합니다. |
| 내부 오류 | 500 | 서버 내부 오류가 발생했습니다. |

---

### 5️⃣-1 베스트 상품 조회 API

`GET /api/v1/products/best`

| 에러 상황 | 에러 코드 | 에러 메시지 |
| --- | --- | --- |
| 내부 오류 | 500 | 서버 내부 오류가 발생했습니다. |