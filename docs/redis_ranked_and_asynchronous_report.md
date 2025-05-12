# Redis 기반 TOP10 인기 상품 조회 시스템 설계 및 개발 보고서


1. 개요
   🔍 목적
   상품 주문 데이터를 기반으로 실시간 인기 상품 TOP10을 사용자에게 제공하는 기능을 구현한다.

높은 요청 처리 성능과 정확한 순위 계산을 위해 Redis Sorted Set(ZSet)을 활용한다.

2. 시스템 설계
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

3. 개발 상세
   ✅ 주문 완료 시 점수 누적
   OrderEntity가 PAID로 변경되면, 포함된 모든 OrderProductEntity에 대해 Redis ZSet 점수 증가

예시 코드:

```java
redisTemplate.opsForZSet().incrementScore("ranking:daily:2025-05-11", productId.toString(), quantity);
```
✅ 일간 인기 상품 조회

Redis에서 상위 N개 상품 ID 조회 (ZREVRANGE)

DB에서 해당 ID들의 상품 정보 조회

DTO 변환 후 응답 반환

4. 성능 및 장점

   | 항목         | 효과                                                                 |
   |--------------|----------------------------------------------------------------------|
   | 응답 속도     | Redis에서 인기 상품 ID 조회는 평균 1~2ms 이내                         |
   | 데이터 일관성 | 주문 → 점수 증가 흐름을 트랜잭션 후에 분리하여 비동기화 가능           |
   | 확장성        | 일간 외에도 주간/월간 키 추가 가능 (`ranking:weekly:{yyyy-WW}` 등)     |
   | 정렬 정확도   | 점수 기반 내림차순 정렬로 사용자 신뢰도 상승                           |



5. 테스트 결과
   ✅ E2E 테스트를 통해 상품 주문 시 Redis 점수가 실제로 증가하는지 검증

✅ /api/v1/products/top API 호출 시 Redis 점수 순서대로 정렬된 상품이 응답됨

✅ 상품이 10개 이상일 때 정확히 size만큼 반환되는지 확인

6. 회고 및 개선 방향
   👍 잘한 점
- Redis ZSet을 통해 정렬된 데이터를 빠르게 제공 가능
- 서비스/레포지토리 분리 설계를 통해 DIP 원칙을 지켜 유지보수가 쉬움
- 테스트 코드에서 Redis 점수까지 검증하여 안정성 확보

⚠️ 아쉬운 점
- TTL 설정 시 중복 호출로 인해 expire가 반복 설정됨 (최적화 여지 있음)
- Redis 장애 대비 fallback 로직이 아직 없음 → 추후 DB 기반 랭킹으로 대체 가능성 고려 필요
- Redis에 저장된 데이터는 실시간성이 강하지만, 데이터 동기화 실패 시 보완 필요



# Redis 기반 선착순 쿠폰 발급 시스템 설계 및 개발 보고서

## 1. 개요
🔍 **목적**  
쿠폰을 선착순으로 발급받는 기능을 기존 spinLock 방식에서 더 나은 성능으로 제공하기 위해 Redis를 활용한 고성능 쿠폰 발급 시스템을 구축하였다.  
쿠폰 발급의 중복 방지, 재고 관리, 높은 동시성 처리 성능을 확보하는 것이 주요 목표이다.

## 2. 시스템 설계
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

## 3. 개발 상세

✅ **발급 흐름 요약**

1. 쿠폰 발급 요청 시 이벤트 발행 (`CouponIssueEvent`)
2. Redis Set으로 중복 발급 여부 확인 (`SADD`)
3. Redis List에서 재고 차감 (`LPOP`)
4. 쿠폰 정보 조회 → UserCouponEntity 생성
5. 트랜잭션으로 DB 저장 (`@Transactional`)

✅ **예시 코드 (핵심 로직)**

```java
Long added = redisTemplate.opsForSet().add("coupon:issued:" + couponId, userId.toString());
if (added == null || added == 0) return; // 중복

String token = redisTemplate.opsForList().leftPop("coupon:stock:" + couponId);
if (token == null) {
    redisTemplate.opsForSet().remove("coupon:issued:" + couponId, userId.toString());
    return; // 재고 없음
}

UserCouponEntity entity = UserCouponEntity.save(...);
couponService.issuedCoupon(entity); // 트랜잭션 저장
```

✅ **비동기 설정**

- `@Async("taskExecutor")`로 스레드 풀 병렬 처리
- `@EventListener`로 이벤트 기반 구조화
- ThreadPoolTaskExecutor로 최대 동시 요청 처리 제어

## 4. 성능 및 장점

| 항목           | 효과                                                            |
|----------------|---------------------------------------------------------------|
| 요청 처리 성능   | Redis 기반 자료구조만 사용하므로 평균 응답 1~2ms 수준 유지 가능 ( 테스트시 약 1.29 초 발생) |
| 중복 방지 정확도 | Redis Set으로 O(1) 중복 확인 및 삭제 가능                                |
| 확장성          | TTL로 Redis 키 자동 만료 → 일회성 이벤트 쿠폰 운영에 적합                        |
| 트랜잭션 분리    | 발급 처리 이후에만 DB 저장 → 비동기 구조로 확장 가능                              |
| 테스트 용이성    | Redis, DB 모두 단위 테스트 및 통합 테스트 가능 (동시성 시나리오 포함)                 |

## 5. 테스트 결과

✅ 110명이 동시에 쿠폰 발급 요청을 보내도 Redis 재고가 100개면 **정확히 100명만 성공**  
✅ 중복 요청 시 Redis Set에서 차단되는지 확인  
✅ `Awaitility`로 비동기 발급 결과를 대기하면서 E2E 테스트 수행  
✅ `userCouponRepository.count()` 및 Redis List/Set 사이즈 검증으로 결과 검토

## 6. 회고 및 개선 방향

👍 **잘한 점**
- Redis만으로 락 없이 선착순 쿠폰 발급 처리 성공
- `@Async` 기반 설계로 서비스 확장성 및 응답 속도 확보
- DIP 기반 Repository 추상화 및 서비스-리스너 분리 설계 적용
- 테스트에서 병렬 실행과 성능 측정, 실패 케이스까지 모두 커버
- TTL 설정 시 `expire()`가 매 요청마다 반복되지 않도록 최초만 설정

⚠️ **아쉬운 점**
- Redis 장애 시 fallback 로직 미구현 → 향후 DB 기반 예외 처리 로직 필요
- Redis에 기록된 데이터가 DB 저장 실패 시 동기화되지 않음 → 메시지 큐 또는 저장 전 검증 로직 고려 필요