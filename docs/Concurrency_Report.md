# 동시성 이슈 식별 및 해결 보고서

## 1. 낙관적 락 적용 사례 - 포인트 충전 시스템
🛠 문제 인식
사용자는 포인트를 충전할 수 있으며, 일반적으로 한 시점에 한 유저가 여러 번 포인트 충전을 시도하는 경우는 드뭄

하지만 간헐적으로 동시에 충전 요청이 들어올 경우, 데이터 불일치 위험 발생

### 원인 분석
기본적으로 충전 요청 시 포인트 잔액을 조회한 뒤 계산 후 업데이트

동시에 요청이 들어오면, 조회-계산-업데이트 사이에 데이터가 달라질 수 있음 → Lost Update 발생 가능

포인트가 100 이있고 100 충전할 경우
1. 100 Tx select point = 100
3. 100 Tx update point = 200

2. 101 Tx select point = 100
4. 101 Tx update point = 200
   result point = 200
   이렇게 포인트 충전 동시성 이슈 발생

다만 포인트 충전은 여러 사용자가 동시 접근이 아닌 사용자 1명에게만 해당되어 경합이 낮다고 생각하여 낙관적 락(@Version) 사용 결정

### 해결 방법
JPA 엔티티에 @Version 필드 추가하여 낙관적 락 적용

충전 로직 실행 시 OptimisticLockException 발생할 경우, 1회 재시도 로직 추가


@Version
private Long version;
``` java
public PointResult chargePointWithRetry(ChargePointCommand command) {
        int retry = 1;

        while (retry-- > 0) {
            try {
                return chargePoint(command); // 충전 로직 호출
            } catch (ObjectOptimisticLockingFailureException | OptimisticLockException e) {
                if (retry == 0) {
                    throw new BusinessException(PointErrorCode.CONFLICT);
                }
                try {
                    Thread.sleep(100); // 잠깐 대기 후 재시도
                } catch (InterruptedException ignored) {}
            }
        }

        throw new BusinessException(PointErrorCode.CONFLICT); // 이건 거의 안 터짐
    }
```
### 대안 제시
충전 요청이 더욱 빈번해질 경우에는 Redis 기반 분산 락을 사용하거나

Kafka 등으로 충전 요청을 큐잉하여 직렬 처리하는 구조도 고려 가능

## 2. 비관적 락 적용 사례 - 쿠폰 발급 / 주문 / 주문 취소 시스템
🛠 문제 인식
쿠폰 발급, 주문 처리, 주문 취소의 경우 짧은 시간에 다수의 사용자가 동시에 접근할 수 있음

정확한 수량 제어 및 중복 방지가 반드시 필요함

### 원인 분석
동시에 여러 유저가 동일 쿠폰을 발급받거나, 동일 상품을 주문하거나, 주문을 취소할 경우

재고, 쿠폰 수량, 주문 상태 등의 데이터 정합성이 깨질 위험 존재

해당 상황은 강한 정합성 보장 필요

### 해결 방법
해당 데이터 조회 시 .setLockMode(LockModeType.PESSIMISTIC_WRITE)   사용하여 비관적 락 적용

DB 차원에서 트랜잭션 동안 다른 쓰기/읽기 접근 차단

확실한 동시성 제어 보장

```java
@Override
    public Optional<CouponEntity> findByIdLock(Long couponId) {
        CouponEntity result = queryFactory
            .selectFrom(coupon)
            .where(coupon.id.eq(couponId))
            .setLockMode(LockModeType.PESSIMISTIC_WRITE)  // 락 설정
            .fetchOne();

        return Optional.ofNullable(result);
    }
```
### 대안 제시
성능 병목이 우려된다면, Redis 기반 분산락(Redisson) 또는 Kafka Consumer 단일 처리 방식 고려

쿠폰 발급의 경우, 쿠폰 자체를 사전에 분산하여 캐시 처리하는 구조도 가능 (예: 카카오 쿠폰 방식)

### 결론

| 구분         | 낙관적 락           | 비관적 락                     |
|--------------|---------------------|-------------------------------|
| 적용 대상    | 포인트 충전         | 쿠폰 발급, 주문, 주문 취소   |
| 경합 가능성  | 낮음                | 높음                          |
| 성능 영향    | 적음                | 있음                          |
| 처리 방식    | 충돌 시 재시도      | 충돌 자체 방지               |
| 적합 상황    | 충돌이 드문 시나리오 | 충돌이 자주 발생하는 핵심 로직 |
