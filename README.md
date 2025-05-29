## 이커머스 프로젝트
### Clean + Layered Architecture 설계
![image](https://github.com/user-attachments/assets/a4c2ba66-6fab-4deb-96fe-eae9074fa102)
- 애플리케이션의 핵심은 비즈니스 로직
- 데이터 계층 및 API 계층이 비즈니스 로직을 의존 ( 비즈니스의 Interface 활용 )
- 도메인 중심적인 계층 아키텍처
- Presentation 은 도메인을 API로 서빙, DataSource 는 도메인이 필요로 하는 기능을 서빙
- DIP 🆗 OCP 🆗
  
### 설계 문서

# 1. [요구사항 정의서](docs/Requirements.md) (문서 참조)
# 2. [시퀀스 다이어그램](docs/sequence_diagram.md)
![image](https://github.com/user-attachments/assets/5b7e2da3-eb79-4075-9983-9ad655f7da6f)
![image](https://github.com/user-attachments/assets/295e19b3-d999-4584-a8e3-473609fbbabd)
이하 문서 참조
# 3. [ERD](docs/ERD.md)
![image](https://github.com/user-attachments/assets/d9e3169b-464b-4ed6-b588-e4ee94a4aafc)
# 4. [API 명세서](docs/API_docs.md)
 e-commerce API Docs

![](https://img.shields.io/static/v1?label=&message=GET&color=blue)
![](https://img.shields.io/static/v1?label=&message=POST&color=brightgreen)
![](https://img.shields.io/static/v1?label=&message=PUT&color=orange)
![](https://img.shields.io/static/v1?label=&message=PATCH&color=pink)
![](https://img.shields.io/static/v1?label=&message=DELETE&color=red)

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

이하 문서 참조

# 5. [DB 인덱스 적용 조 성능 개선](docs/DB_indexing_report.md) (자세한 내용 문서 참조)
성능분석
## 인덱스 적용 전후 성능 비교

| 지표               | 인덱스 적용 전       | 인덱스 적용 후 |
|--------------------|-----------------------|----------|
| **실행 시간**       | 164ms                | 3ms      |
| **전체 비용**       | 456,599              | 155      |
| **데이터 검색 방식** | 전체 테이블 스캔      | 인덱스 검색   |
| **처리 행 수**      | 1,000,000건          | 100건     |

# 6. [DB기반 동시성 이슈 개선](docs/Concurrency_Report.md) - 경합 발생 빈도, 기능의 중요도에 따른 낙관적 락, 비관적 락 사용 (자세한 내용 문서 참조)
# 7. [Redis 기반 캐싱 전략](docs/Cache_report.md) (자세한 내용 문서 참조)
- Redis 캐시 적용 성능 향상
![image](https://github.com/user-attachments/assets/4c573a0c-d38c-42d0-b34a-5c5ec2326464)

캐시에 값이 없을 경우 자동으로 DB를 조회 후 캐시에 저장됨
결과가 null인 경우는 캐시에 저장하지 않음
TTL 설정: CacheConfig에서 TTL을 30분으로 설정하여, 일정 시간 후 캐시가 자동 만료되도록 구성함
직렬화/역직렬화 설정: GenericJackson2JsonRedisSerializer 및 JavaTimeModule을 등록하여 LocalDateTime 등 Java 8 시간 API를 포함한 객체가 JSON 형식으로 Redis에 저장되도록 설정함
캐시 무효화 처리: 결제, 주문 등 쿠폰 상태 변경 시에는 다음 어노테이션을 통해 해당 사용자 캐시를 삭제함

### 캐시 적용 목적 인기 상품 리스트는 빈번히 노출되며 실시간으로 변동되지 않기 때문에, 일정 주기로 갱신된 데이터를 Redis에 저장하여 항상 캐시가 유지되도록 설계

#### 캐시 전략

스케줄러 기반 갱신: 인기 상품 조회 캐시 갱신 스케줄러를 매일 00시 30분에 실행되도록 설정  
TTL 설정: 캐시 TTL을 25시간으로 지정하여, 인기 상품 목록이 항상 캐시에 존재하도록 유지  
DB 조회 빈도 최소화: 애플리케이션 전반에서 인기 상품 조회 시 Redis에 저장된 JSON 데이터를 바로 반환

#### 결론 및 기대 효과
항목 효과 보유 쿠폰 캐시 적용 응답 속도 약 1/3 단축, DB 부하 감소 캐시 TTL/무효화 설정 신선한 데이터 유지 및 오염 방지 인기 상품 캐시 전략 항상 캐시 유지, 조회 속도 안정성 확보

# 8. [Redis 자료구조 사용 TOP10 인기 상품 조회 및 Redis Lock 사용 동시성 제어 및 대용량 트래픽 쿠폰 발급 비동기 이벤트 설계](docs/redis_ranked_and_asynchronous_report.md) (문서 참조)
### 시스템 설계  
#### Redis 기반 TOP10 인기 상품 조회  
상품 결제마다 주문 수량만큼 해당 상품의 score를 증가하고, 조회시 레디스 ZSet 자료구조 사용하여 스코어 순서로 10 건의 productid 순차 조회 후 DB 조회한다.  
   🧱 핵심 설계 요소

| 항목             | 내용                                     |
|------------------|----------------------------------------|
| 데이터 저장소     | Redis (Sorted Set, ZSet)               |
| 기준 점수         | 상품별 주문 수량 합계                           |
| 랭킹 키           | `ranking:daily:{yyyy-MM-dd}` 형식의 일자별 키 |
| TTL 설정         | 2일 (일간 기준 데이터 자동 만료)                   |
| 요청 API         | `GET /api/v1/products/top?size=N`      |
| 정렬 기준         | 점수 내림차순 (`ZREVRANGE`)                  |
| 스코어 누적 시점 | 결제 완료 시 `ZINCRBY` 사용하여 주문수량만큼 score 증가 |
| 랭킹 상품 정보     | Redis에서는 ID만 관리, 실제 상품 정보는 DB에서 매핑     |  

### 시스템 설계
#### Redis 기반 선착순 쿠폰 발급 시스템 설계  
쿠폰을 선착순으로 발급받는 기능을 기존 spinLock 방식에서 더 나은 성능으로 제공하기 위해 Redis를 활용한 고성능 쿠폰 발급 시스템을 구축.  
쿠폰 발급의 중복 방지, 재고 관리, 높은 동시성 처리 성능을 확보하는 것이 주요 목표.  

🧱 **핵심 설계 요소**

| 항목             | 내용                                                                 |
|------------------|----------------------------------------------------------------------|
| 데이터 저장소     | Redis (List + Set)                                                   |
| 중복 방지         | Redis Set(`coupon:issued:{couponId}`)으로 userId 중복 체크           |
| 재고 관리         | Redis List(`coupon:stock:{couponId}`)에 재고 수 만큼 토큰 저장       |
| TTL 설정         | 쿠폰 만료일 기준으로 Redis TTL 자동 설정                            |
| 발급 요청 방식     | Spring `@Async + @EventListener` 기반 비동기 이벤트 처리             |
| 발급 처리 순서     | 중복 확인 → 재고 차감 → DB 저장 순으로 처리                          |
| 트랜잭션 처리      | DB 저장 로직은 별도 서비스에 위임하여 `@Transactional` 적용           |

성능 및 장점

| 항목         | 효과                                                               |
|------------|------------------------------------------------------------------|
| 요청 처리 성능   | Redis 기반 자료구조 및 Lua + EVALSHA 적용으로 평균 응답 1~2ms 유지 (테스트 시 약 0.9초) |
| 원자성 확보     | Lua 내부에서 중복 체크 + 재고 차감 + 발급 기록을 동시에 처리하여 race condition 제거 |
| 중복 방지 정확도  | Redis Set으로 O(1) 중복 확인 및 삭제 가능                                   |
| 확장성        | TTL로 Redis 키 자동 만료 → 일회성 이벤트 쿠폰 운영에 적합                           |
| 트랜잭션 분리    | 발급 처리 이후에만 DB 저장 → 비동기 구조로 확장 가능                                 |
| 테스트 용이성    | Redis, DB 모두 단위 테스트 및 통합 테스트 가능 (동시성 시나리오 포함)                    |

(자세한 내용 문서 참조)
# 9. [서비스 MSA 확장 서비스 설계 및 분산트랜잭션](docs/MSA_Architecture_Change_Design_Report.md) (문서 참조)
# 10. [주문 정보 외부 전송 kafka 메시지 처리](docs/kafka_basic_learning.md) 
- 카프카에 대한 기본 학습 자료 정리 및 주문 프로세스 완료 시 비동기 이벤트 발행 후 kafka 메시지로 주문 정보 외부 전송 을 구현하였으며,
  이렇게 구현한 이유는 현재는 주문 프로세스 완료 후 처리하는 기능은 주문 정보 외부 전송 뿐이지만 추후 알림 서비스 등의 확정성을 고려하여 설계 및 구현
# 11. [카프카 활용 선착순 쿠폰 발급 대용량 트래픽 비동기 처리 설계 및 구현](docs/kafka_design_report.md) (자세한 내용 문서 참조)
  기존 Spring 내장 이벤트 기반으로 처리되던 선착순 쿠폰 발급 로직을 Kafka 기반의 비동기 메시징 시스템으로 변경하여 대용량 트래픽에도 견딜 수 있는 확장성, 장애 분리, 메시지 유실 방지, 재처리 용이성을 확보
  ![image](https://github.com/user-attachments/assets/bca4898b-0d6c-44cb-8d92-0cbbd1c4ece9)

  ### Kafka Producer
```java
public void publishCompleted(CouponIssueCommand command) {
        try {
            String key = String.valueOf(command.couponId()); // couponId 기준 파티션
            kafkaTemplate.send(TOPIC, key, command).get(); // Future.get()으로 대기
            log.info("카프카 쿠폰 발행 메시지 발행 성공");
        } catch (Exception e) {
            log.error("카프카 발행 중 예외 발생", e);
        }
    }
```
### Kafka Consumer
```java
@KafkaListener(
        topics = "coupon.issue",
        groupId = "coupon-consumer-group",
        containerFactory = "couponKafkaListenerContainerFactory"
    )
    public void consume(CouponIssueCommand command, Acknowledgment ack) {
        try {
            couponService.couponIssued(command);
            // 수동 커밋
            ack.acknowledge();
        } catch (Exception e) {
            // 예외를 삼키면 안 되고 반드시 던져야 DLQ 전송됨
            throw new RuntimeException("쿠폰 발급 처리 실패", e);
        }
    }
```


