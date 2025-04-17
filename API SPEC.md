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

|  필드명   | 데이터 타입 |        설명         |  필수여부  | 유효성 검사                |
|:------:|:------:|:-----------------:|:------:|:----------------------|
| userId | Number | 포인트를 충전하는 사용자 식별자 | **필수** | 양의 정수                 | 
| amount | Number |  충전하고자 하는 포인트 금액  | **필수** | 0보다 크면서 1,000,000원 이하 |

**Example Reuqest Body**

```json
{
  "userId": 1,
  "amount": 100000
}
```

#### Response

<details markdown="1">
<summary>200 OK : 성공적으로 충전된 경우</summary>

```json
{
  "code": 200,
  "status": "OK",
  "message": "요칭이 정상적으로 처리되었습니다.",
  "data": {
    "userId": 1,
    "balance": 1000000
  }
}
```

</details>

<details markdown="1">
<summary>409 Conflict : 1회 충전 금액을 초과한 경우</summary>
</details>

<details markdown="1">
<summary>409 Conflict : 누적 충전 금액 초과</summary>
</details>

</details>
<br>

### 포인트 조회 API

> ![](https://img.shields.io/static/v1?label=&message=GET&color=blue) <br>
> `/api/v1/points?userId={userId}`

<details markdown="1">

<summary>스펙 상세</summary>

#### Paramters

**Query Params**

|  필드명   | 데이터 타입 |        설명        |  필수여부  | 유효성 검사 |
|:------:|:------:|:----------------:|:------:|:-------|
| userId | Number | 포인트를 조회하는 사용자 ID | **필수** | 양의 정수  | 

#### Response

<details markdown="1">
<summary>200 OK : 성공적으로 조회된 경우</summary>

```json
{
  "code": 200,
  "status": "OK",
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

|   필드명   | 데이터 타입 |       설명       |  필수여부  | 유효성 검사 |
|:-------:|:------:|:--------------:|:------:|:-------|
| orderId | Number | 사용자가 주문한 주문 ID | **필수** | 양의 정수  |

<details markdown="1">
<summary>200 OK : 성공적으로 조회된 경우</summary>

```json
{
  "code": 204,
  "status": "No Content",
  "message": "요청이 정상적으로 처리되었습니다."
}
```

</details>

<details markdown="1">
<summary>409 Conflict : 결제 금액이 포인트보다 크면 실패</summary>
</details>

<details markdown="1">
<summary>409 Conflict : 주문 상태가 FAIL(결제 불가 건)</summary>
</details>

</details>
<br>

## Product

---

### 상품 목록 조회 API

> ![](https://img.shields.io/static/v1?label=&message=GET&color=blue) <br>
> `/api/v1/products`

<details markdown="1"> 
<summary>스펙 상세</summary>

#### Response

<details markdown="1">
<summary>200 OK : 성공적으로 조회된 경우</summary>

```json
{
  "code": 200,
  "status": "OK",
  "message": "요청이 정상적으로 처리되었습니다.",
  "data": {
    "products": [
      {
        "id": 1,
        "name": "Macbook Pro",
        "price": 2000000,
        "stock": 10
      },
      {
        "id": 2,
        "name": "iPhone 12",
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
> /api/v1/products/best

<details markdown="1">
<summary>스펙 상세</summary>

#### Response

<details markdown="1">
<summary>200 OK : 성공적으로 조회된 경우</summary>

```json
{
  "code": 200,
  "status": "OK",
  "message": "요청이 정상적으로 처리되었습니다.",
  "data": [
    {
      "id": 1,
      "name": "ice americano",
      "price": 1000,
      "sales": 100,
      "stock": 100
    },
    {
      "id": 2,
      "name": "iPhone 12",
      "price": 1200000,
      "sales": 90,
      "stock": 100
    }
  ]
}
```

</details>
</details>
<br>

## Order

---

### 주문 API

> ![](https://img.shields.io/static/v1?label=&message=POST&color=brightgreen) <br>
> `/api/v1/orders`

<details markdown="1">
<summary>스펙 상세</summary>

### Parameter

#### Body

|          필드명           | 데이터 타입 |               설명                |  필수여부  | 유효성 검사                    |
|:----------------------:|:------:|:-------------------------------:|:------:|:--------------------------|
|        `userId`        | Number |       주문을 생성한 사용자의 고유 ID        | **필수** | 양의 정수                     | 
|     `userCouponId`     | Number | 사용자가 적용한 쿠폰 ID (없으면 null 또는 생략) | **선택** | 양의 정수                     |
|      `orderItems`      | Array  |      주문 항목 (상품 ID와 수량의 배열)      | **필수** | 최소 1개 이상의 항목이 있어야 함       |
| `orderItems.productId` | Number | 사용자가 적용한 쿠폰 ID (없으면 null 또는 생략) | **필수** | 양의 정수                     |
| `orderItems.quantity`  | Number | 사용자가 적용한 쿠폰 ID (없으면 null 또는 생략) | **필수** | 양의 정수 (최소 1개 이상의 수량이어야 함) |

**Example Reuqest Body**

```json
{
  "userId": 1,
  "userCouponId": 1,
  "orderItems": [
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

```json
{
  "code": 201,
  "status": "Created",
  "message": "주문이 성공적으로 완료되었습니다.",
  "data": {
    "orderId": 1
  }
}
```

</details>

<details markdown="1">
<summary>409 Conflict : 쿠폰을 적용하였으나 보유한 쿠폰이 아니면 주문이 실패한 경우</summary>
</details>

<details markdown="1">
<summary>409 Conflict : 쿠폰이 유효한 기간이 아니라서 주문이 실패한 경우</summary>
</details>

<details markdown="1">
<summary>409 Conflict : 이미 사용된 쿠폰을 적용하려고 해서 주문이 실패한 경우</summary>
</details>

<details markdown="1">
<summary>409 Conflict : 재고가 부족해서 주문이 실패한 경우</summary>
</details>
</details>
<br>

## Coupon

---

### 사용자 보유 쿠폰 조회 API

> ![](https://img.shields.io/static/v1?label=&message=GET&color=blue) <br>
> `/api/v1/coupons?userId={userId}`

<details markdown="1">
<summary>스펙 상세</summary>

#### Paramters

**Query Params**

|  필드명   | 데이터 타입 |       설명        |  필수여부  | 유효성 검사 |
|:------:|:------:|:---------------:|:------:|:-------|
| userId | Number | 쿠폰을 조회하는 사용자 ID | **필수** | 양의 정수  |

<details markdown="1">
<summary>200 OK : 성공적으로 조회된 경우</summary>

```json
{
  "code": 200,
  "status": "OK",
  "message": "요청이 정상적으로 처리되었습니다.",
  "data": {
    "userId": 1,
    "coupons": [
      {
        "id": 1,
        "title": "10% 할인 쿠폰",
        "discountType": "RATE",
        "discountRate": 10,
        "startDate": "2025-08-01",
        "endDate": "2025-08-31"
      },
      {
        "id": 2,
        "title": "10,000원 할인 쿠폰",
        "discountType": "AMOUNT",
        "discountAmount": 10000,
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

|   필드명    | 데이터 타입 |       설명        |  필수여부  | 유효성 검사 |
|:--------:|:------:|:---------------:|:------:|:-------|
|  userId  | Number | 쿠폰을 발급받는 사용자 ID | **필수** | 양의 정수  |
| couponId | Number |   발급받을 쿠폰 ID    | **필수** | 양의 정수  |

**Example Request Body**

```json
{
  "userId": 1,
  "couponId": 1
}
```

<details markdown="1">
<summary>200 OK : 쿠폰 발급을 받은 경우</summary>

```json
{
  "code": 201,
  "status": "Created",
  "message": "요청이 정상적으로 처리되었습니다.",
  "data": {}
}
```

</details>

<details markdown="1">
<summary>409 Conflict : 쿠폰의 잔여 수량이 남지 않아 쿠폰 발급이 실패한 경우</summary>
</details>

<details markdown="1">
<summary>409 Conflict : 이미 쿠폰을 발급 받아 쿠폰 발급이 실패한 경우</summary>
</details>
</details>